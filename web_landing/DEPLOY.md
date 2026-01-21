# Publicar la web y verla en el celular

## Opción rápida (para probar ya): Netlify (manual)

1) Entrá a https://app.netlify.com/
2) Arrastrá la carpeta `web_landing/` (o el contenido) al área de “Deploy manually”.
3) Netlify te da una URL pública tipo `https://xxxxx.netlify.app`.
4) Abrí esa URL desde el celular.

## Para que se actualice solo (deploy automático)

La forma correcta es: **GitHub + Netlify (Continuous Deployment)**.

1) Subí este proyecto a un repo en GitHub.
2) En Netlify: **Add new site → Import an existing project**.
3) Conectá GitHub y elegí el repo.
4) Netlify va a detectar `netlify.toml` y va a publicar automáticamente `web_landing/`.
5) A partir de ahí, cada vez que hagas cambios y los subas (push), Netlify redeployea solo.

Recomendación para no mezclar cambios del sistema POS con la web:
- Usar un branch dedicado (ej: `landing`) y conectar Netlify a ese branch.

Archivo incluido para esto: `netlify.toml` (publish = `web_landing`).

## Opción alternativa: GitHub Pages

1) Subí tu proyecto a un repositorio en GitHub.
2) En el repo: **Settings → Pages**.
3) Elegí el branch (ej: `main`) y la carpeta (root o `/docs`).
4) Si querés publicar `web_landing/`, lo más simple es copiarlo a `/docs`.

## Para que aparezca en Google

Tener el sitio online no garantiza que salga al buscarlo. Para indexarlo:

1) Comprá un dominio (ej: `vantasoft.com.ar` o `vantasoft.com`).
2) Apuntá el dominio al hosting (Netlify o GitHub Pages).
3) Entrá a **Google Search Console** y agregá tu propiedad.
4) Verificá el dominio (DNS) y pedí indexación:
   - “Inspección de URL” → “Solicitar indexación”.

Google puede tardar desde horas a días en mostrar resultados.

## SEO mínimo incluido

- `robots.txt` permite indexar la home y bloquea `/templates/`.
- `site.webmanifest` + favicon.
- Meta tags básicos (title/description/og).
