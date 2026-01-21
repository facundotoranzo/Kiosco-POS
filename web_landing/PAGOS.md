# Pagos (Naranja X) y planes

## Qué se puede hacer y qué no

Para cobrar con tarjeta en un sitio web, no conviene (ni corresponde) pedir y procesar números de tarjeta desde tu propio formulario sin un proveedor que cumpla PCI.
La práctica estándar es usar un **checkout hospedado** (la pantalla del proveedor) o un **widget/tokenización** del proveedor.

En Naranja X, las soluciones publicadas suelen ser **Link de pago** / **QR** / **Toque** (cobros) y mencionan integración vía APIs para QR en comercios.

## Recomendación para tu objetivo

### Opción A: Naranja X (Link de pago / QR)

- El botón “Comprar” crea una orden en tu sistema (plan + cliente).
- Generás un link/QR en Naranja X y se lo mostrás/enviás.
- Al confirmarse el pago, se marca como pagado y se activa el plan.

Limitación: la UX de “ingresar tarjeta dentro de tu web” depende de que exista un checkout oficial embebible.

### Opción B: Proveedor con checkout web (más directo para tarjeta)

Si querés que el cliente ingrese tarjeta en un flujo web estándar, lo más común es integrar un proveedor con checkout web.
Ejemplo: Mercado Pago ofrece checkout web y permite cobrar con tarjeta.

## Cómo se implementa la lógica de planes

Base:

- `plan_requests`: el cliente pide cambio de plan desde el portal.
- `subscriptions/{uid}`: el estado final (plan y estado) que controla el acceso.

Estados sugeridos:

- `trial`: prueba
- `active`: pago OK
- `past_due`: vencido/no pago

## Siguiente paso

1) Definir el proveedor real (Naranja X link/QR o checkout web).
2) Implementar “crear pago” y “webhook” (cuando el proveedor confirme).
3) Al confirmarse el pago: actualizar `subscriptions/{uid}`.

