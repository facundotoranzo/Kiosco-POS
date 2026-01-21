# Landing y flujo de venta

## Objetivo

- Botón: **Probar gratis 3 días** (descarga del ZIP).
- Sección: **Planes mensuales**.
- Compra: pago validado (webhook) y entrega de **código de activación** para esa instalación.

La idea es entregar **un solo instalador/EXE** y habilitar el plan con un código.

## Flujo recomendado (pago + activación)

1) **Prueba gratis**
- El usuario descarga el ZIP, abre el EXE y puede usar 3 días.
- En el programa: “Configuración → Licencia” muestra el **ID de instalación**.

2) **Compra de plan**
- En la web, el cliente elige el plan.
- La web pide el **ID de instalación** (para emitir la licencia de ese equipo).
- La web crea el pago (ej: Mercado Pago) con metadata: plan + install_id + email/teléfono.

3) **Verificación de pago**
- El backend recibe el webhook del proveedor.
- Reconsulta al proveedor por API y confirma que el pago está **approved / acreditado**.
- Recién ahí se emite la licencia.

4) **Entrega**
- El backend genera el **código de licencia** (plan + vencimiento) y lo entrega:
  - en pantalla (si el usuario está logueado), y/o
  - por email/WhatsApp.

5) **Activación en el programa**
- El cliente abre “Configuración → Licencia”, pega el código y queda activo.

## Planes

- $10.000/mes: 2 usuarios / 2 cajas
- $15.000/mes: 4 usuarios / 4 cajas
- $25.000/mes: 10 usuarios / 10 cajas

## Nota importante sobre “100% seguro”

Ningún método es 100% irreversible (pueden existir contracargos o pagos revertidos). La mejor práctica es:

- Entregar la licencia cuando esté **approved/acreditado**.
- Emitir licencias con vencimiento (mensual) y renovar con cada pago.
- Si el pago se revierte, se corta en la próxima validación/renovación.

## Suscripción y no pago

- La app guarda los datos del cliente (productos, cajas, etc.) en su base local.
- Si no se paga el mes, la licencia expira y el programa se bloquea, pero **los datos no se borran**.
- Para volver a habilitar, se entrega un nuevo código (renovación) y sigue usando los mismos datos.

## Portal de clientes (Google + chat)

El portal usa Firebase (Auth + Firestore + Storage):

1) Crear proyecto en Firebase Console.
2) Activar Authentication → Google.
3) Crear Firestore Database.
4) Activar Storage.
5) Completar `firebase-config.js` con la config del proyecto.
6) Configurar reglas (referencia en `firebase/`).

Páginas:

- `portal.html`: login Google + chat + solicitud de cambio de plan.
- `admin.html`: lista de clientes + chat + estado de suscripción.
