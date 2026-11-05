# Favoritos Android 1.0 — projeto APK

Este projeto é uma adaptação do `PWA_Favoritos_v3.html` enviado pelo usuário, usando **Java + Android WebView**.

## Recursos implementados
- Importar HTML, TXT ou PDF do celular por seletor de arquivos Android.
- Copiar o conteúdo para o **IndexedDB privativo do aplicativo** (e armazenar metadados no localStorage).
- Abrir HTML importado sem precisar manter o arquivo na pasta original (arquivos autossuficientes).
- Abrir URLs HTTPS dentro do próprio aplicativo **em vez de iniciar Chrome externo**.
- Exportar backup JSON (inclui arquivos importados) e importar o backup do PWA antigo.
- Exportar individualmente arquivos importados com seu nome original, inclusive depois que o original foi movido.
- Buscar favoritos por nome, nome do arquivo ou URL; editar, apagar e personalizar cores/ícones.
- Botões nativos Voltar e Favoritos; pasta `.github` presente no ZIP.

## Criar APK no GitHub, só com o celular
1. Crie um repositório vazio (de preferência privado para projetos pessoais).
2. Importe este ZIP **sem pasta extra na raiz** no aplicativo de envio para GitHub.
3. Confira se `.github/workflows/android-apk.yml` aparece no repositório.
4. Abra o repositório > Actions > **Gerar Favoritos Android APK**.
5. Abra a execução verde e, em **Artifacts**, baixe **Favoritos-Android-APK**.
6. Extraia o APK de dentro do artefato e instale no Android.

O ZIP entregue neste pacote é **código-fonte do Android**, não o APK compilado. A compilação acontece na infraestrutura do GitHub. O workflow usa o SDK Android já instalado no runner e tenta instalar a plataforma 35 se ausente (sem o passo `android-actions/setup-android`, que havia falhado em outros projetos).

## Migrar os favoritos PWA antigos
1. Abra o **PWA antigo** no celular > toque em `⬇️ Exportar backup` (gera `.json`).
2. Instale o APK e abra **Favoritos Android** > toque em `⬆️ Importar backup`.
3. Escolha o arquivo `.json` e confirme. O app grava cópias locais em seu próprio IndexedDB.
4. Verifique a abertura e exportação dos arquivos e mantenha o backup fora do app.

**Importante**: o novo APK não lê automaticamente os dados privados do PWA/Chrome. Mesmo que os arquivos HTML originais tenham sido movidos, um backup correto do PWA pode conter seu texto. **Não desinstale o PWA nem limpe seus dados antes de confirmar o backup.**

## Limitações
- Arquivos HTML com dependências externas (CSS, JS, imagens por URL) podem precisar de rede ou falhar se recursos relativos não foram importados. A cópia é apenas do arquivo selecionado, não uma pasta de recursos.
- PDFs grandes (> 1,5 MB) e textos/HTML grandes (> 2 MB) eram recusados pelo código PWA original, limite ainda presente nesta versão.
- Exportação nativa por documento usa uma ponte JS de até 7 MB por exportação.
- URLs salvas são atalhos, **não cópias offline**.
- O armazenamento privativo pode ser perdido ao **desinstalar**, **limpar dados**, ou migrar entre assinaturas/apps. Faça backups JSON.
- Este APK tem pacote `com.exemplo.favoritos`. Atualizações precisam conservar **exatamente o mesmo pacote e chave de assinatura**. O APK de debug criado pelo GitHub Actions pode receber chaves debug diferentes em cada compilação; antes de atualizar, faça backup e, depois, configure uma chave estável para upgrades sem desinstalar.
- A versão em WebView usa tecnologia do Android System WebView (base Chromium) mas roda no contexto do **aplicativo Favoritos**, separado do Chrome. Se o Guardião de Dados bloquear este APK, links não funcionarão, mesmo com Chrome liberado. Libere o app, se necessário.
- Importe apenas **arquivos HTML confiáveis**: páginas com scripts podem executar código e acessar informações armazenadas na mesma origem do visualizador. Uma versão futura deve usar um visualizador isolado para HTML de fontes desconhecidas.
- O código é um projeto gerado para testes; **não foi compilado/testado num Android real neste ambiente**.

## Privacidade
Não exige conta nem servidor próprio; conteúdos ficam no armazenamento local privativo do APK. Links da Internet acessam os sites normalmente. O Android WebView guarda cookies/cache em seu próprio perfil.

## Alterações futuras
Criar lista/filtros, pastas/categorias, exportação em lote ZIP, miniaturas/ícones, links offline para sites autorizados, e navegador em WebView isolada para HTML de origens desconhecidas.
