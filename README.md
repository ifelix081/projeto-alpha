# projeto-alfa

# MVP — Sistema de Autenticação Web

**Projeto de portfólio** | Equipe: 2 desenvolvedores
**Stack:** NestJS + TypeORM + SQLite (dev) / PostgreSQL (prod) · HTML/CSS/JS puro · JWT · Gmail SMTP

---

## 1. Visão Geral

Sistema web completo de cadastro e login com confirmação de conta por e-mail e recuperação de senha. Após autenticado, o usuário acessa uma página protegida simples (dashboard).

**Objetivo:** aprender na prática autenticação segura, envio de e-mails transacionais, arquitetura modular com NestJS e deploy de aplicação full stack.

**O que NÃO entra no MVP:** login social (Google/GitHub), 2FA, perfil de usuário editável, painel admin. Podem virar melhorias futuras (v2).

---

## 2. Funcionalidades do MVP

| # | Funcionalidade | Descrição |
|---|---------------|-----------|
| F1 | Cadastro | Nome, e-mail e senha. Conta criada como "não confirmada". |
| F2 | E-mail de confirmação | Link com token enviado ao e-mail. Ao clicar, conta é ativada. |
| F3 | Login | E-mail + senha. Retorna JWT. Bloqueado se conta não confirmada. |
| F4 | Esqueci minha senha | Usuário informa e-mail e recebe link de reset (token expira em 1h). |
| F5 | Redefinir senha | Página com formulário de nova senha, validada pelo token. |
| F6 | Dashboard protegido | Página simples acessível só com JWT válido. Mostra nome do usuário e botão de logout. |

---

## 3. Histórias de Usuário

- Como visitante, quero me cadastrar com e-mail e senha para criar minha conta.
- Como usuário recém-cadastrado, quero receber um e-mail de confirmação para ativar minha conta.
- Como usuário confirmado, quero fazer login para acessar o dashboard.
- Como usuário que esqueceu a senha, quero receber um link por e-mail para redefini-la.
- Como usuário logado, quero fazer logout para encerrar minha sessão.

---

## 4. Modelo de Dados

### Tabela `users`

| Campo | Tipo | Observação |
|-------|------|-----------|
| id | uuid | PK |
| name | varchar | |
| email | varchar | único |
| password_hash | varchar | bcrypt, nunca senha em texto puro |
| is_confirmed | boolean | default: false |
| confirmation_token | varchar (nullable) | hash do token de confirmação |
| reset_token | varchar (nullable) | hash do token de reset |
| reset_token_expires | datetime (nullable) | expiração de 1h |
| created_at | datetime | |

> Dica de segurança: armazene o **hash** dos tokens no banco, não o token em si. O token puro só vai no link do e-mail.

---

## 5. API — Endpoints

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | /auth/register | Cria usuário e envia e-mail de confirmação | — |
| GET | /auth/confirm?token= | Confirma a conta | — |
| POST | /auth/login | Valida credenciais e retorna JWT | — |
| POST | /auth/forgot-password | Envia e-mail com link de reset | — |
| POST | /auth/reset-password | Redefine a senha com o token | — |
| GET | /users/me | Retorna dados do usuário logado | JWT |

**Regras de negócio importantes:**
- Login de conta não confirmada retorna erro 403 com mensagem clara.
- "Esqueci senha" sempre responde sucesso, mesmo se o e-mail não existir (evita enumeração de usuários).
- Senha mínima de 8 caracteres, validada com class-validator.
- Token de reset é invalidado após o uso.

---

## 6. Estrutura do Backend (NestJS)

```
src/
├── main.ts
├── app.module.ts
├── users/
│   ├── users.module.ts
│   ├── users.service.ts
│   └── user.entity.ts
├── auth/
│   ├── auth.module.ts
│   ├── auth.controller.ts
│   ├── auth.service.ts
│   ├── jwt.strategy.ts
│   └── jwt-auth.guard.ts
└── mail/
    ├── mail.module.ts
    └── mail.service.ts
```

**Bibliotecas:** @nestjs/typeorm, typeorm, sqlite3, @nestjs/jwt, @nestjs/passport, passport-jwt, bcrypt, class-validator, nodemailer, @nestjs/config

---

## 7. Frontend (HTML/CSS/JS puro)

```
frontend/
├── index.html          → login
├── register.html       → cadastro
├── forgot.html         → esqueci a senha
├── reset.html          → redefinir senha (recebe ?token= na URL)
├── confirmed.html      → "conta confirmada com sucesso"
├── dashboard.html      → página protegida
├── css/style.css
└── js/
    ├── api.js          → funções fetch centralizadas (base URL da API)
    └── auth.js         → salvar/ler/remover JWT do localStorage
```

O dashboard verifica o token ao carregar: chama `GET /users/me`; se der 401, redireciona para o login.

---

## 8. Divisão de Trabalho (2 pessoas)

A divisão é por feature, não por camada — assim os dois aprendem backend e frontend.

**Dev A — Cadastro e Confirmação**
- Setup do projeto NestJS + TypeORM + entidade User
- Endpoints: register, confirm
- Módulo de e-mail (Gmail SMTP + nodemailer)
- Páginas: register.html, confirmed.html

**Dev B — Login e Recuperação**
- Configuração do JWT (strategy + guard)
- Endpoints: login, forgot-password, reset-password, /users/me
- Páginas: index.html (login), forgot.html, reset.html, dashboard.html

**Em conjunto:** modelagem inicial do banco, style.css, code review um do outro via Pull Request, deploy.

> Workflow Git sugerido: branch `main` protegida, uma branch por feature (`feat/register`, `feat/login`...), merge só via PR com revisão do colega. Isso também conta pontos no portfólio.

---

## 9. Roadmap

**Semana 1 — Base**
Setup do NestJS, entidade User, cadastro com bcrypt, login com JWT, guard protegendo /users/me. Testar tudo via Insomnia/Postman.

**Semana 2 — E-mails**
Configurar Senha de App do Gmail (exige 2FA na conta Google), e-mail de confirmação com token, bloqueio de login para conta não confirmada.

**Semana 3 — Recuperação de senha + Frontend**
Fluxo completo de forgot/reset. Construção das páginas HTML consumindo a API.

**Semana 4 — Deploy + Polimento**
- Trocar SQLite por PostgreSQL gratuito (Neon ou Supabase) — com TypeORM é só mudar a config de conexão
- API no Render ou Railway (variáveis de ambiente: JWT_SECRET, credenciais SMTP, DATABASE_URL)
- Frontend no Vercel ou GitHub Pages
- Configurar CORS no NestJS para o domínio do frontend
- README caprichado com prints, diagrama do fluxo e link do projeto no ar

---

## 10. Variáveis de Ambiente (.env)

```
DATABASE_URL=          # vazio em dev (SQLite local), Neon em prod
JWT_SECRET=
JWT_EXPIRES_IN=1d
MAIL_USER=             # seu Gmail
MAIL_PASS=             # Senha de App (não a senha normal da conta)
FRONTEND_URL=          # usado nos links dos e-mails
```

> Nunca commitar o .env. Adicione ao .gitignore desde o primeiro commit.

---

## 11. Critérios de Conclusão do MVP

- [ ] Usuário consegue se cadastrar e recebe e-mail real
- [ ] Conta só loga após confirmação
- [ ] Fluxo de recuperação de senha funciona ponta a ponta
- [ ] Dashboard inacessível sem token válido
- [ ] Projeto no ar com link funcionando
- [ ] README com instruções de instalação e prints
