# MVP — Sistema de Autenticação Web (Java)

**Projeto de portfólio** | Equipe: 2 desenvolvedores (aprendendo Java no projeto)
**Stack:** Java 21 + Spring Boot 3 · Spring Data JPA · H2 (dev) / PostgreSQL (prod) · Spring Security + JWT · Spring Mail (Gmail SMTP) · HTML/CSS/JS puro

---

## 1. Visão Geral

Sistema web completo de cadastro e login com confirmação de conta por e-mail e recuperação de senha. Após autenticado, o usuário acessa uma página protegida simples (dashboard).

**Objetivo:** aprender Java e Spring Boot na prática, cobrindo autenticação segura, envio de e-mails transacionais, arquitetura em camadas e deploy de aplicação full stack.

**O que NÃO entra no MVP:** login social (Google/GitHub), 2FA, perfil editável, painel admin. Ficam para a v2.

---

## 2. Funcionalidades do MVP

| # | Funcionalidade | Descrição |
|---|---------------|-----------|
| F1 | Cadastro | Nome, e-mail e senha. Conta criada como "não confirmada". |
| F2 | E-mail de confirmação | Link com token enviado ao e-mail. Ao clicar, conta é ativada. |
| F3 | Login | E-mail + senha. Retorna JWT. Bloqueado se conta não confirmada. |
| F4 | Esqueci minha senha | Usuário informa e-mail e recebe link de reset (token expira em 1h). |
| F5 | Redefinir senha | Página com formulário de nova senha, validada pelo token. |
| F6 | Dashboard protegido | Página acessível só com JWT válido. Mostra nome do usuário e logout. |

---

## 3. Histórias de Usuário

- Como visitante, quero me cadastrar com e-mail e senha para criar minha conta.
- Como usuário recém-cadastrado, quero receber um e-mail de confirmação para ativar minha conta.
- Como usuário confirmado, quero fazer login para acessar o dashboard.
- Como usuário que esqueceu a senha, quero receber um link por e-mail para redefini-la.
- Como usuário logado, quero fazer logout para encerrar minha sessão.

---

## 4. Modelo de Dados

### Tabela `users` (entidade `User`)

| Campo | Tipo Java | Observação |
|-------|-----------|-----------|
| id | UUID | PK, @GeneratedValue |
| name | String | |
| email | String | único (@Column(unique = true)) |
| passwordHash | String | BCrypt, nunca senha em texto puro |
| isConfirmed | boolean | default: false |
| confirmationToken | String (nullable) | hash do token de confirmação |
| resetToken | String (nullable) | hash do token de reset |
| resetTokenExpires | LocalDateTime (nullable) | expiração de 1h |
| createdAt | LocalDateTime | @CreationTimestamp |

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
- Login de conta não confirmada retorna 403 com mensagem clara.
- "Esqueci senha" sempre responde sucesso, mesmo se o e-mail não existir (evita enumeração de usuários).
- Senha mínima de 8 caracteres, validada com Bean Validation (@Size, @NotBlank).
- Token de reset é invalidado após o uso.

---

## 6. Estrutura do Backend (Spring Boot)

Arquitetura em camadas, padrão do ecossistema Spring:

```
src/main/java/com/seugrupo/authapp/
├── AuthAppApplication.java
├── config/
│   ├── SecurityConfig.java        → regras de acesso, filtro JWT, CORS
│   └── JwtAuthFilter.java         → valida o token a cada requisição
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── JwtService.java            → gerar e validar tokens
│   └── MailService.java           → envio de e-mails
├── repository/
│   └── UserRepository.java        → interface JpaRepository
├── entity/
│   └── User.java
└── dto/
    ├── RegisterRequest.java       → records com validação
    ├── LoginRequest.java
    ├── LoginResponse.java
    └── ResetPasswordRequest.java

src/main/resources/
├── application.properties         → config geral
├── application-dev.properties     → H2
└── application-prod.properties    → PostgreSQL
```

**Dependências (via Spring Initializr — start.spring.io):**
Spring Web, Spring Data JPA, Spring Security, Validation, Spring Mail, H2 Database, PostgreSQL Driver, Lombok (opcional, reduz boilerplate). Para JWT, adicionar manualmente a lib `jjwt` (io.jsonwebtoken) no pom.xml.

> O **Spring Initializr** (start.spring.io) gera o projeto pronto com as dependências marcadas — é o ponto de partida da etapa 1.

---

## 7. Frontend (HTML/CSS/JS puro)

Idêntico à versão original — o frontend não muda, pois só consome a API:

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

Divisão por feature, não por camada — os dois aprendem backend e frontend.

**Fase 0 (juntos):** como os dois estão aprendendo Java, façam a etapa 1 do roadmap em par (pair programming). Configurar Spring Security pela primeira vez sozinho é frustrante; em dupla é aprendizado.

**Dev A — Cadastro e Confirmação**
- Endpoints: register, confirm
- MailService (Gmail SMTP + Spring Mail)
- Páginas: register.html, confirmed.html

**Dev B — Login e Recuperação**
- Endpoints: login, forgot-password, reset-password, /users/me
- JwtService e refinamento do filtro JWT
- Páginas: index.html (login), forgot.html, reset.html, dashboard.html

**Em conjunto:** entidade User, SecurityConfig, style.css, code review via Pull Request, deploy.

> Workflow Git: branch `main` protegida, uma branch por feature (`feat/register`, `feat/login`...), merge só via PR com revisão do colega. Conta pontos no portfólio.

---

## 9. Roadmap (6 semanas — ritmo de quem está aprendendo Java)

**Semana 1 — Fundamentos**
Instalar JDK 21 + IntelliJ IDEA Community. Aprender o essencial de Java: classes, objetos, interfaces, collections, exceptions. Gerar o projeto no Spring Initializr e entender a estrutura. Fazer um "Hello World" REST (um controller que responde JSON).

**Semana 2 — CRUD e banco**
Entidade User com JPA, UserRepository, cadastro salvando no H2 com senha em BCrypt. Testar via Insomnia/Postman. Acessar o console do H2 no navegador para ver os dados.

**Semana 3 — Segurança**
SecurityConfig, JwtService, filtro JWT, endpoint de login e /users/me protegido. É a semana mais difícil — façam juntos.

**Semana 4 — E-mails**
Senha de App do Gmail (exige 2FA na conta Google), MailService, e-mail de confirmação com token, bloqueio de login para conta não confirmada.

**Semana 5 — Recuperação de senha + Frontend**
Fluxo forgot/reset completo. Páginas HTML consumindo a API.

**Semana 6 — Deploy + Polimento**
- Perfil prod com PostgreSQL gratuito (Neon ou Supabase)
- API no Render ou Railway (deploy via Dockerfile ou buildpack Java)
- Frontend no Vercel ou GitHub Pages
- CORS configurado no SecurityConfig para o domínio do frontend
- README caprichado com prints, diagrama do fluxo e link do projeto no ar

---

## 10. Configuração (application.properties)

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:file:./data/authapp
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

# application-prod.properties (valores via variáveis de ambiente)
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=update

# comum
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USER}
spring.mail.password=${MAIL_PASS}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.frontend-url=${FRONTEND_URL}
```

> Nunca commitar segredos. Em dev, usem variáveis de ambiente da IDE ou um arquivo local fora do Git.

---

## 11. Critérios de Conclusão do MVP

- [ ] Usuário consegue se cadastrar e recebe e-mail real
- [ ] Conta só loga após confirmação
- [ ] Fluxo de recuperação de senha funciona ponta a ponta
- [ ] Dashboard inacessível sem token válido
- [ ] Projeto no ar com link funcionando
- [ ] README com instruções de instalação e prints

---

## 12. Materiais de Apoio

- Documentação oficial do Spring Boot e guias em spring.io/guides (curtos e práticos)
- Spring Initializr: start.spring.io
- Baeldung (baeldung.com) — referência número 1 para tutoriais de Spring Security e JWT
- Console H2: acessível em /h2-console durante o dev
