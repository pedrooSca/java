import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../../main/webapp/js/app.js', import.meta.url), 'utf8');
const flush = async () => { for (let i = 0; i < 8; i++) await new Promise(setImmediate); };
function element() {
    return { dataset: {}, listeners: {}, className: '', textContent: '',
        addEventListener(type, action) { this.listeners[type] = action; },
        reportValidity() { return true; }, reset() { this.resetCalled = true; },
        querySelector() { return this.button; }, querySelectorAll() { return []; } };
}
function setup(page, stored = {}, respond) {
    const elements = Object.fromEntries(['api-content', 'api-message', 'login-form', 'user-form', 'logout', 'account-profile']
        .map(id => ['#' + id, element()]));
    elements['#api-content'].dataset.page = page;
    elements['#login-form'].button = element();
    elements['#user-form'].button = element();
    const details = {open:true};
    elements['#user-form'].closest = () => details;
    elements['#login-form'].elements = {email:{value:''},senha:{focus() {}}};
    const state = new Map(Object.entries(stored));
    const redirects = [], calls = [];
    vm.runInNewContext(source, {
        document: { querySelector: selector => elements[selector], querySelectorAll: () => [] },
        sessionStorage: { getItem: key => state.get(key) ?? null, setItem: (key,value) => state.set(key,value), removeItem: key => state.delete(key) },
        location: { search: '', replace: url => redirects.push(url) }, URLSearchParams,
        FormData: class { constructor(form) {this.signup = form === elements['#user-form'];} *[Symbol.iterator]() { if(this.signup) yield ['nome','Novo Leitor']; yield ['email','admin@teste.local']; yield ['senha','Senha de teste!123']; } },
        fetch: async (url, options) => { calls.push({url,options}); return respond(url, options); }
    });
    return { elements, state, redirects, calls };
}

{
    const test = setup('index', {}, () => assert.fail('Não deve consultar dados sem token'));
    assert.deepEqual(test.redirects, ['login.jsp']);
    console.log('OK: Tela sem autenticação redireciona para login sem consultar a API');
}
{
    const test = setup('login', {}, async () => ({ status:200, ok:true, json:async()=>({token:'novo-token',perfil:'ADMIN',email:'admin@teste.local'}) }));
    await test.elements['#login-form'].listeners.submit({ preventDefault() {} });
    assert.equal(test.calls[0].url, 'api/auth/login');
    assert.equal(test.calls[0].options.method, 'POST');
    assert.equal(test.calls[0].options.headers.Authorization, undefined);
    assert.equal(test.state.get('livraria-token'), 'novo-token');
    assert.equal(test.state.get('livraria-profile'), 'ADMIN');
    assert.equal(test.state.get('livraria-email'), 'admin@teste.local');
    assert.equal(test.elements['#login-form'].resetCalled, true);
    assert.deepEqual(test.redirects, ['index.jsp']);
    console.log('OK: Login salva token e perfil e limpa o formulário');
}
{
    const test = setup('login', {}, async () => ({ status:401, ok:false, json:async()=>({erro:'E-mail ou senha inválidos.'}) }));
    await test.elements['#login-form'].listeners.submit({ preventDefault() {} });
    assert.equal(test.state.has('livraria-token'), false);
    assert.equal(test.elements['#api-message'].textContent, 'E-mail ou senha inválidos.');
    assert.equal(test.redirects.length, 0);
    console.log('OK: Login inválido exibe o erro sem guardar credenciais');
}
{
    const test = setup('index', {'livraria-token':'expirado','livraria-profile':'ADMIN'},
        async () => ({ status:401, ok:false, json:async()=>({erro:'Expirado'}) }));
    await flush();
    assert.equal(test.calls[0].options.headers.Authorization, 'Bearer expirado');
    assert.equal(test.state.has('livraria-token'), false);
    assert.deepEqual(test.redirects, ['login.jsp']);
    console.log('OK: API rejeita token expirado e a interface exige novo login');
}
{
    const test = setup('login', {'livraria-token':'token','livraria-profile':'ADMIN','livraria-email':'admin@teste.local'}, () => assert.fail());
    test.elements['#logout'].listeners.click();
    assert.equal(test.state.size, 0);
    assert.deepEqual(test.redirects, ['login.jsp']);
    console.log('OK: Sair limpa os dados da sessão da aba');
}

{
    const test = setup('login', {}, async () => ({ status:201, ok:true, json:async()=>({mensagem:'Cadastro concluído'}) }));
    await test.elements['#user-form'].listeners.submit({ preventDefault() {} });
    assert.equal(test.calls[0].url, 'api/auth/cadastro');
    assert.equal(test.calls[0].options.method, 'POST');
    assert.equal(JSON.parse(test.calls[0].options.body).nome, 'Novo Leitor');
    assert.equal(test.state.has('livraria-token'), false);
    assert.equal(test.elements['#login-form'].elements.email.value, 'admin@teste.local');
    assert.equal(test.elements['#user-form'].resetCalled, true);
    assert.equal(test.elements['#user-form'].closest().open, false);
    assert.match(test.elements['#api-message'].textContent, /Cadastro concluído/);
    console.log('OK: Cadastro público envia os dados, limpa a senha e prepara o login');
}
{
    const test = setup('login', {}, async () => ({ status:409, ok:false, json:async()=>({erro:'E-mail já cadastrado.'}) }));
    await test.elements['#user-form'].listeners.submit({ preventDefault() {} });
    assert.equal(test.elements['#user-form'].resetCalled, undefined);
    assert.equal(test.elements['#api-message'].textContent, 'E-mail já cadastrado.');
    assert.equal(test.state.has('livraria-token'), false);
    console.log('OK: Falha de cadastro mantém o formulário para correção');
}
