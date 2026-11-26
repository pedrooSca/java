import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../../main/webapp/js/app.js', import.meta.url), 'utf8');
const flush = async () => { for (let i = 0; i < 8; i++) await new Promise(setImmediate); };

for (const profile of ['CONSULTA', 'ADMIN']) {
    const elements = Object.fromEntries([
        'api-content', 'api-message', 'review-form', 'review-author', 'livroId',
        'comentario', 'reviews', 'all-reviews', 'admin-form', 'admin-action'
    ].map(id => ['#' + id, {
        id, hidden: false, value: '', listeners: {},
        addEventListener(type, action) { this.listeners[type] = action; },
        replaceChildren() {}, add() {}, reportValidity() { return true; },
        querySelector() { return this.button; }
    }]));
    elements['#api-content'].dataset = { page: 'avaliar' };
    const adminAction = elements['#admin-action'];
    // Simula os elementos abrangidos pelo seletor de permissões real.
    elements['#api-content'].querySelectorAll = selector =>
        [elements['#review-form'], elements['#admin-form']]
            .filter(form => !selector.includes(':not(#' + form.id + ')')).concat(adminAction);
    elements['#review-form'].button = {};
    const calls = [];
    vm.runInNewContext(source, {
        document: {
            querySelector: selector => elements[selector],
            querySelectorAll: () => [adminAction]
        },
        sessionStorage: { getItem: key => key === 'livraria-token' ? 'token-teste' : key === 'livraria-email' ? 'leitor@teste.local' : profile },
        location: { search: '?livroId=3' }, URLSearchParams,
        Option: class {},
        FormData: class {
            *[Symbol.iterator]() {
                yield ['usuarioId', '999']; yield ['livroId', '3'];
                yield ['nota', '5']; yield ['comentario', 'Excelente'];
            }
        },
        fetch: async (url, options) => {
            calls.push({ url, options });
            const body = url === 'api/usuarios' ? [{ id: 2, nome: 'Leitor' }]
                : url === 'api/livros' ? [{ id: 3, titulo: 'Livro', autor: 'Autor' }] : [];
            return { status: options.method === 'POST' ? 201 : 200, ok: true, json: async () => body };
        }
    });
    await flush();
    assert.equal(elements['#review-form'].hidden, false);
    assert.match(elements['#review-author'].textContent, /leitor@teste.local/);
    assert.equal(calls.some(call => call.url === 'api/usuarios'), false);
    assert.equal(elements['#admin-form'].hidden, profile !== 'ADMIN');
    assert.equal(adminAction.hidden, profile !== 'ADMIN');
    elements['#comentario'].value = 'Excelente';
    await elements['#review-form'].listeners.submit({ preventDefault() {} });
    const post = calls.find(call => call.options.method === 'POST');
    assert.equal(post.url, 'api/avaliacoes');
    assert.equal(post.options.headers.Authorization, 'Bearer token-teste');
    assert.deepEqual(JSON.parse(post.options.body), { livroId: 3, nota: 5, comentario: 'Excelente' });
    assert.equal(elements['#comentario'].value, '');
    assert.match(elements['#api-message'].textContent, /sucesso/);
    console.log('OK: ' + profile + ' visualiza e envia avaliações com as demais permissões preservadas');
}
