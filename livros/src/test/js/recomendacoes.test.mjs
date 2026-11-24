import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

// Executa o app.js real com respostas HTTP controladas, sem banco ou navegador.
const source = await readFile(new URL('../../main/webapp/js/app.js', import.meta.url), 'utf8');
const flush = async () => { for (let i = 0; i < 8; i++) await new Promise(setImmediate); };

class Element {
    constructor() {
        this.listeners = {}; this.buttons = []; this.items = [];
        this.value = ''; this.disabled = false; this.hidden = false;
    }
    addEventListener(type, handler) { this.listeners[type] = handler; }
    fire(type) { return this.listeners[type]?.({ preventDefault() {} }); }
    replaceChildren(...items) { this.items = items; }
    add(item) { this.items.push(item); }
    reportValidity() { return true; }
    querySelector() { return this.button; }
    querySelectorAll() { return this.buttons; }
    set textContent(value) { this.text = value; this.html = ''; this.buttons = []; }
    get textContent() { return this.text; }
    set innerHTML(value) {
        this.html = value;
        this.buttons = [...value.matchAll(/data-remove="(\d+)"/g)].map(match => {
            const button = new Element(); button.dataset = { remove: match[1] }; return button;
        });
    }
    get innerHTML() { return this.html; }
}

async function setup() {
    const elements = Object.fromEntries([
        'api-content', 'api-message', 'active-user', 'preference-form',
        'preference-genre', 'preferences', 'user-email', 'recommendation-title', 'books'
    ].map(id => ['#' + id, new Element()]));
    elements['#api-content'].dataset = { page: 'recomendacoes' };
    elements['#preference-form'].button = new Element();
    const pending = [], mutations = [];
    const users = [{id:1,nome:'Ana',email:'ana@example.com'}, {id:2,nome:'Bia',email:'bia@example.com'}];
    const genres = [{id:1,nome:'Fantasia'}, {id:2,nome:'Romance'}];
    vm.runInNewContext(source, {
        document: { querySelector: selector => elements[selector] },
        location: { search: '' }, URLSearchParams,
        Option: class { constructor(text, value) { this.text = text; this.value = value; } },
        sessionStorage: { getItem: key => key === 'livraria-token' ? 'token-teste' : key === 'livraria-profile' ? 'ADMIN' : null },
        fetch: (url, options) => {
            assert.equal(options.headers.Authorization, "Bearer token-teste");
            if (url === 'api/usuarios') return Promise.resolve({ status:200, ok:true, json:async()=>users });
            if (url === 'api/generos') return Promise.resolve({ status:200, ok:true, json:async()=>genres });
            return new Promise((resolve, reject) => {
                const request = {
                    url, method:options.method, reject,
                    respond(body, status=200) { resolve({ status, ok:status<400, json:async()=>body }); }
                };
                pending.push(request);
                if (options.method !== 'GET') mutations.push(request);
            });
        }
    });
    await flush();
    function batch(id) {
        const take = url => {
            const index = pending.findIndex(r => r.url === url && r.method === 'GET');
            assert.ok(index >= 0, 'Requisição esperada: ' + url);
            return pending.splice(index, 1)[0];
        };
        return [take('api/usuarios/'+id+'/preferencias'), take('api/usuarios/'+id+'/recomendacoes'), take('api/medias')];
    }
    function respond(batch, id) {
        batch[0].respond([{id:1,nome:'Fantasia'}]);
        batch[1].respond([{livroId:id,titulo:'Livro '+id,autor:'Autor',generoNome:'Fantasia'}]);
        batch[2].respond({[id]:4.5});
    }
    async function select(id) {
        elements['#active-user'].value = id;
        elements['#active-user'].fire('change');
        await flush();
    }
    return {elements, pending, mutations, batch, respond, select};
}

export async function runRegressionTests() {
    const passed = [];
    // B termina antes de A: A não pode sobrescrever a tela nem criar botões para Ana.
    {
        const h = await setup(), a = h.batch('1');
        await h.select('2'); const b = h.batch('2');
        assert.equal(h.elements['#preference-form'].button.disabled, true);
        h.respond(b, 2); await flush(); h.respond(a, 1); await flush();
        assert.equal(h.elements['#user-email'].textContent, 'bia@example.com');
        assert.match(h.elements['#books'].innerHTML, /Livro 2/);
        assert.match(h.elements['#books'].innerHTML, /★ 4\.5/);
        assert.doesNotMatch(h.elements['#books'].innerHTML, /Sem avaliações/);
        passed.push('Resposta antiga descartada e média real exibida');

        // Durante DELETE, impedir outra seleção e uma segunda gravação.
        h.elements['#preferences'].buttons[0].fire('click'); await flush();
        assert.equal(h.elements['#active-user'].disabled, true);
        assert.equal(h.elements['#preference-form'].button.disabled, true);
        h.elements['#preference-form'].fire('submit'); await flush();
        assert.equal(h.mutations.length, 1);
        assert.equal(h.mutations[0].url, 'api/usuarios/2/preferencias/1');
        assert.equal(h.mutations[0].method, 'DELETE');
        h.mutations[0].respond(null, 204); await flush();
        h.respond(h.batch('2'), 2); await flush();
        assert.equal(h.elements['#active-user'].disabled, false);
        assert.equal(h.elements['#preference-form'].button.disabled, false);
        passed.push('Remoção usa o leitor correto e bloqueia gravações concorrentes');

        // O callback de um botão antigo também não pode gravar após a troca.
        const oldButton = h.elements['#preferences'].buttons[0];
        await h.select('1'); oldButton.fire('click'); await flush();
        assert.equal(h.mutations.length, 1);
        h.respond(h.batch('1'), 1); await flush();
        h.elements['#preference-genre'].value = '2';
        h.elements['#preference-form'].fire('submit'); await flush();
        assert.equal(h.mutations[1].url, 'api/usuarios/1/preferencias/2');
        assert.equal(h.mutations[1].method, 'PUT');
        h.mutations[1].reject(new Error('offline')); await flush();
        assert.equal(h.elements['#active-user'].disabled, false);
        assert.equal(h.elements['#preference-form'].button.disabled, false);
        assert.match(h.elements['#api-message'].textContent, /conectar/);
        passed.push('Botão antigo inativo, adição correta e recuperação após falha de gravação');
    }
    // Falha antiga não pode esconder os dados do leitor atual.
    {
        const h = await setup(), a = h.batch('1');
        await h.select('2'); h.respond(h.batch('2'), 2); await flush();
        a[0].reject(new Error('offline')); a[1].respond([]); a[2].respond({}); await flush();
        assert.equal(h.elements['#user-email'].textContent, 'bia@example.com');
        assert.equal(h.elements['#api-message'].textContent, '');
        assert.equal(h.elements['#preference-form'].button.disabled, false);
        passed.push('Falha de consulta antiga ignorada');
    }
    // Falha atual mantém as ações bloqueadas; seleção vazia limpa a tela.
    {
        const h = await setup(), a = h.batch('1');
        a[0].reject(new Error('offline')); a[1].respond([]); a[2].respond({}); await flush();
        assert.equal(h.elements['#preference-form'].button.disabled, true);
        h.elements['#preference-form'].fire('submit'); await flush();
        assert.equal(h.mutations.length, 0);
        await h.select('');
        assert.equal(h.elements['#preference-form'].hidden, true);
        assert.equal(h.elements['#user-email'].textContent, '');
        assert.equal(h.elements['#recommendation-title'].textContent, 'Livros para você');
        passed.push('Falha atual bloqueia alterações e seleção vazia limpa dados');
    }
    return passed;
}

// Executável sem dependências: node livros/src/test/js/recomendacoes.test.mjs
if (typeof process !== 'undefined' && process.argv[1] && import.meta.url === (await import('node:url')).pathToFileURL(process.argv[1]).href) {
    for (const name of await runRegressionTests()) console.log('OK: ' + name);
}
