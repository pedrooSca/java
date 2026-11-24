'use strict';
(() => {
    const content = document.querySelector('#api-content');
    if (!content) return;
    // Impede envio tradicional mesmo se o carregamento inicial falhar.
    content.addEventListener('submit', event => event.preventDefault());
    const page = content.dataset.page;
    const tokenKey = 'livraria-token';
    const profileKey = 'livraria-profile';
    const token = sessionStorage.getItem(tokenKey);
    const admin = sessionStorage.getItem(profileKey) === 'ADMIN';
    if (page !== 'login' && !token) {
        location.replace('login.jsp');
        return;
    }
    function logout() {
        sessionStorage.removeItem(tokenKey);
        sessionStorage.removeItem(profileKey);
        sessionStorage.removeItem('livraria-message');
        location.replace('login.jsp');
    }
    document.querySelector('#logout')?.addEventListener('click', logout);
    const accountProfile = document.querySelector('#account-profile');
    if (accountProfile) accountProfile.textContent = admin ? 'Administrador' : 'Consulta';
    if (!admin && page !== 'login') {
        content.querySelectorAll('form:not(#search-form), [data-admin]').forEach(element => element.hidden = true);
        document.querySelectorAll('[data-admin], a[href="novo-livro.jsp"], a[href="auditoria"], a[href="auditoria.jsp"]').forEach(element => element.hidden = true);
        if (['novo-livro', 'editar-livro', 'auditoria'].includes(page)) {
            location.replace('livros');
            return;
        }
    }
    const params = new URLSearchParams(location.search);
    const $ = s => document.querySelector(s);
    const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    function message(text, success = false) {
        const box = $('#api-message'); box.className = 'alert ' + (success ? 'alert-success' : 'alert-error'); box.textContent = text;
    }
    async function api(path, method = 'GET', data) {
        const options = {method, headers:{Accept:'application/json'}};
        if (token && path !== 'auth/login') options.headers.Authorization = 'Bearer ' + token;
        if (data !== undefined) {options.headers['Content-Type']='application/json'; options.body=JSON.stringify(data);}
        let response;
        try {response = await fetch('api/' + path,options);} catch (_) {throw new Error('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.');}
        const body = response.status === 204 ? null : await response.json();
        if (response.status === 401 && page !== 'login') {
            logout();
            throw new Error('Sua sessão expirou. Entre novamente.');
        }
        if (!response.ok) throw new Error(body.erro || 'Não foi possível concluir a operação.');
        return body;
    }
    function options(select, items, selected = '', label = x => x.nome, placeholder = 'Selecione...') {
        select.replaceChildren(new Option(placeholder,''));
        items.forEach(x => select.add(new Option(label(x),x.id ?? x.livroId)));
        select.value=String(selected ?? '');
    }
    function form(selector, action) {
        const element=$(selector);
        element.addEventListener('submit', async event => {
            event.preventDefault();
            if (!element.reportValidity()) return;
            const button=element.querySelector('button[type="submit"],button:not([type])');
            if(button) button.disabled=true;
            try { await action(Object.fromEntries(new FormData(element)),element); }
            catch(e) {message(e.message);}
            finally {if(button) button.disabled=false;}
        });
    }
    const date = value => value ? new Date(value).toLocaleString('pt-BR') : '—';
    function cards(books, means = {}, editable = false) {
        $('#books').innerHTML=books.map(b => {
            const id = b.livroId ?? b.id;
            if (page === 'index') return '<div class="rec-card"><div style="display:flex;justify-content:space-between;margin-bottom:8px"><span class="badge-genre">'+escape(b.generoNome)+'</span><span style="color:var(--accent)">✦ Rec.</span></div><h3 style="font-size:1.1rem;margin-bottom:6px">'+escape(b.titulo)+'</h3><p>por '+escape(b.autor)+'</p></div>';
            return '<article class="book-card"><div><div class="book-header"><span class="book-id">#'+id+'</span><span class="badge-genre">'+escape(b.generoNome)+'</span></div><h2 class="book-title">'+escape(b.titulo)+'</h2><p class="book-author">Por '+escape(b.autor)+'</p><div class="book-badges">'+(b.anoPublicacao != null ? '<span class="badge-year">Ano: '+escape(b.anoPublicacao)+'</span>' : '')+(b.isbn ? '<span>ISBN: '+escape(b.isbn)+'</span>' : '')+'</div><div class="book-rating">'+(means[id] != null ? '★ '+Number(means[id]).toFixed(1) : '☆ Sem avaliações')+'</div></div><div class="book-footer"><div class="book-actions">'+(editable ? '<a class="btn btn-secondary btn-sm" href="editar-livro.jsp?id='+id+'">Editar</a>' : '')+'<a class="btn btn-secondary btn-sm" href="avaliar.jsp?livroId='+id+'">Avaliar</a></div>'+(editable ? '<button class="btn btn-danger btn-sm" data-delete="'+id+'">Excluir</button>' : '')+'</div></article>';
        }).join('');
        $('#books').querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click',async()=>{
            const b=books.find(x=>(x.livroId ?? x.id)===Number(button.dataset.delete));
            if(!confirm('Excluir o livro "'+b.titulo+'"? As avaliações vinculadas também serão removidas e a exclusão ficará na auditoria.'))return;
            button.disabled=true;
            try {await api('livros/'+button.dataset.delete,'DELETE');await catalog();message('Livro excluído com sucesso.',true);}catch(e){message(e.message);button.disabled=false;}
        }));
    }
    function reviews(items) {
        $('#reviews').innerHTML=items.length ? items.map(r=>'<div class="review-card card"><div><strong>'+escape(r.usuarioNome)+'</strong> <span class="review-stars">'+'★'.repeat(r.nota)+'☆'.repeat(5-r.nota)+'</span><p>sobre <em>'+escape(r.livroTitulo)+'</em></p><p>'+escape(r.comentario)+'</p></div><small>'+date(r.dataAvaliacao)+'</small></div>').join('') : '<div class="card">Nenhuma avaliação registrada ainda.</div>';
    }
    function stats(selector,values) {$(selector).innerHTML=values.map(([value,label])=>'<div class="stat-card"><div class="stat-value">'+value+'</div><div class="stat-label">'+label+'</div></div>').join('');}
    async function catalog() {
        const query=new URLSearchParams(new FormData($('#search-form')));
        const [books,means]=await Promise.all([api('livros?'+query),api('medias')]);
        history.replaceState(null,'',location.pathname+(query.toString() ? '?'+query : ''));
        $('#result-count').textContent=books.length+' livro(s) encontrado(s)';cards(books,means,admin);
    }
    async function loadUsers() {
        const users=await api('usuarios');
        $('#users').innerHTML=users.length ? users.map(u=>'<div class="card"><h3>'+escape(u.nome)+'</h3><p>'+escape(u.email)+'</p><small>Cadastrado em: '+date(u.dataCadastro)+'</small><div>'+ (u.generosPreferidos || []).map(g=>'<span class="badge-genre">'+escape(g.nome)+'</span>').join(' ')+'</div><a class="btn btn-secondary btn-sm" href="recomendacoes.jsp?usuarioId='+u.id+'">Ver Recomendações</a></div>').join('') : '<div class="card">Nenhum usuário cadastrado ainda.</div>';
    }
    async function init() {
        if (page === 'login') {
            form('#login-form', async (data, element) => {
                const acesso = await api('auth/login', 'POST', data);
                sessionStorage.setItem(tokenKey, acesso.token);
                sessionStorage.setItem(profileKey, acesso.perfil);
                element.reset();
                location.replace('index.jsp');
            });
            form('#user-form', async (data, element) => {
                await api('auth/cadastro', 'POST', data);
                $('#login-form').elements.email.value = data.email;
                element.reset();
                element.closest('details').open = false;
                $('#login-form').elements.senha.focus();
                message('Cadastro concluído! Entre com seu e-mail e senha.', true);
            });
            return;
        }
        if(page==='livros') {
            options($('#search-form select'),await api('generos'),params.get('generoId'),undefined,'Todos os Gêneros');
            $('#search-form [name=q]').value=params.get('q') || '';
            form('#search-form',catalog);await catalog();
        } else if(page==='novo-livro' || page==='editar-livro') {
            const edit=page==='editar-livro';
            const f=$('#book-form');
            f.querySelector('button').disabled=true;
            const genres=await api('generos');
            options(f.elements.generoId,genres);
            f.elements.anoPublicacao.max=new Date().getFullYear()+1;
            if(edit) {
                const id=params.get('id');
                if(!id || !/^[1-9][0-9]*$/.test(id)) throw new Error('Informe um identificador de livro válido.');
                const book=await api('livros/'+id);
                ['titulo','autor','isbn','anoPublicacao','generoId'].forEach(k=>f.elements[k].value=book[k] ?? '');
            }
            f.querySelector('button').disabled=false;
            form('#book-form',async data=>{
                data.generoId=Number(data.generoId);data.anoPublicacao=data.anoPublicacao==='' ? null : Number(data.anoPublicacao);
                await api('livros'+(edit ? '/'+params.get('id') : ''),edit ? 'PUT' : 'POST',data);
                sessionStorage.setItem('livraria-message',edit ? 'Livro atualizado com sucesso.' : 'Livro cadastrado com sucesso.');location.assign('livros');
            });
        } else if(page==='usuarios') {
            await loadUsers();
            $('#genres').innerHTML=(await api('generos')).map(g=>'<p><span class="badge-genre">'+escape(g.nome)+'</span><br>'+escape(g.descricao)+'</p>').join('') || 'Nenhum gênero cadastrado.';
        } else if(page==='avaliar') {
            const [users,books]=await Promise.all([api('usuarios'),api('livros')]);
            options($('#usuarioId'),users);options($('#livroId'),books,params.get('livroId'),x=>x.titulo+' — '+x.autor);
            const load=async()=>{const id=$('#livroId').value;reviews(await api('avaliacoes'+(id ? '?livroId='+id : '')));};
            $('#livroId').addEventListener('change',()=>load().catch(e=>message(e.message)));
            $('#all-reviews').addEventListener('click',()=>{$('#livroId').value='';load().catch(e=>message(e.message));});
            await load();
            form('#review-form',async data=>{['usuarioId','livroId','nota'].forEach(k=>data[k]=Number(data[k]));await api('avaliacoes','POST',data);$('#comentario').value='';await load();message('Avaliação registrada com sucesso.',true);});
        } else if(page==='recomendacoes') {
            const [users,genres]=await Promise.all([api('usuarios'),api('generos')]);
            const activeUser=$('#active-user');
            const preferenceForm=$('#preference-form');
            let loadVersion=0,loadedUserId=null,loading=false,mutating=false;
            options(activeUser,users,params.get('usuarioId') || users[0]?.id);
            function updateControls() {
                const blocked=!admin || loading || mutating || !loadedUserId;
                activeUser.disabled=mutating;
                $('#preference-genre').disabled=blocked;
                preferenceForm.querySelector('button').disabled=blocked;
                $('#preferences').querySelectorAll('button').forEach(b=>b.disabled=blocked);
            }
            async function load() {
                const version=++loadVersion;
                const id=activeUser.value;
                loadedUserId=null;loading=true;updateControls();
                preferenceForm.hidden=!admin || !id;
                $('#user-email').textContent='';
                $('#recommendation-title').textContent=id ? 'Carregando recomendações...' : 'Livros para você';
                $('#preferences').textContent=id ? 'Carregando preferências...' : 'Cadastre um usuário para definir preferências.';
                cards([]);message('');
                try {
                    if(!id) return;
                    const [preferences,books,means]=await Promise.all([
                        api('usuarios/'+id+'/preferencias'),
                        api('usuarios/'+id+'/recomendacoes'),
                        api('medias')
                    ]);
                    // Uma resposta antiga não pode substituir o leitor escolhido mais recentemente.
                    if(version!==loadVersion || id!==activeUser.value) return;
                    const user=users.find(u=>u.id===Number(id));
                    $('#user-email').textContent=user.email;
                    $('#recommendation-title').textContent='Livros para '+user.nome+' ('+books.length+')';
                    $('#preferences').innerHTML=preferences.map(g=>'<span class="badge-genre">'+escape(g.nome)+' <button type="button" data-remove="'+g.id+'" aria-label="Remover preferência '+escape(g.nome)+'">×</button></span>').join('') || 'Nenhuma preferência registrada.';
                    options($('#preference-genre'),genres.filter(g=>!preferences.some(p=>p.id===g.id)));
                    $('#preferences').querySelectorAll('button').forEach(b=>b.addEventListener('click',()=>changePreference(id,b.dataset.remove,'DELETE')));
                    cards(books,means);loadedUserId=id;
                } catch(e) {
                    if(version===loadVersion && id===activeUser.value) {
                        $('#preferences').textContent='Não foi possível carregar as preferências.';
                        $('#recommendation-title').textContent='Recomendações indisponíveis';
                        message(e.message);
                    }
                } finally {
                    if(version===loadVersion) {loading=false;updateControls();}
                }
            }
            async function changePreference(id,genreId,method) {
                if(!admin || loading || mutating || !id || id!==loadedUserId || id!==activeUser.value) return;
                mutating=true;updateControls();
                try {
                    await api('usuarios/'+id+'/preferencias/'+genreId,method);
                    await load();
                    if(loadedUserId===id) message('Preferências atualizadas.',true);
                } catch(e) {message(e.message);}
                finally {mutating=false;updateControls();}
            }
            activeUser.addEventListener('change',load);
            preferenceForm.addEventListener('submit',event=>{
                event.preventDefault();
                if(preferenceForm.reportValidity()) changePreference(activeUser.value,$('#preference-genre').value,'PUT');
            });
            await load();
        } else if(page==='auditoria') {
            const logs=await api('auditoria');
            stats('#audit-stats',[[logs.length,'Eventos Registrados'],[logs.filter(l=>l.acao==='UPDATE').length,'Atualizações'],[logs.filter(l=>l.acao==='DELETE').length,'Exclusões']]);
            $('#audit').innerHTML=logs.map(l=>'<tr><td>'+l.id+'</td><td><span class="badge-action '+(l.acao==='UPDATE'?'badge-update':'badge-delete')+'">'+escape(l.acao)+'</span></td><td>'+l.livroId+'</td><td>'+escape(l.tituloAnterior ?? '—')+'</td><td>'+escape(l.tituloNovo ?? 'Livro removido')+'</td><td>'+date(l.dataAlteracao)+'</td></tr>').join('') || '<tr><td colspan="6">Nenhum evento de auditoria registrado ainda.</td></tr>';
        } else if(page==='index') {
            const data=await api('resumo');
            stats('#home-stats',[[data.totalLivros,'Livros no acervo'],[data.totalGeneros,'Gêneros literários'],[data.totalUsuarios,'Leitores cadastrados'],[data.totalAvaliacoes,'Avaliações registradas']]);
            $('#recommendation-title').textContent=data.usuario ? 'Recomendações para '+data.usuario.nome : 'Recomendações';cards(data.recomendacoes);reviews(data.avaliacoes);
        }
        const saved=sessionStorage.getItem('livraria-message');if(saved){message(saved,true);sessionStorage.removeItem('livraria-message');}
    }
    init().catch(e=>message(e.message));
})();
