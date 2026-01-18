package ar.ed.unlu.modelo;

public enum Mensajes {

    HOLA("¡Hola a todos!"),
    LISTO("Estoy listo para jugar."),
    ESPERANDO("Esperando al resto..."),
    BUENA_SUERTE("¡Buena suerte equipo!"),

    BAJA_ROJO("Tengo una carta baja de color rojo."),
    BAJA_AZUL("Tengo una carta baja de color azul."),
    BAJA_AMARILLO("Tengo una carta baja de color amarillo."),
    BAJA_VERDE("Tengo una carta baja de color verde."),
    INTERMEDIA_ROJO("Tengo una carta intermedia de color rojo."),
    INTERMEDIA_AZUL("Tengo una carta intermedia de color azul."),
    INTERMEDIA_AMARILLO("Tengo una carta intermedia de color amarillo."),
    INTERMEDIA_VERDE("Tengo una carta intermedia de color verde."),
    ALTA_ROJO("Tengo una carta alta de color rojo."),
    ALTA_AZUL("Tengo una carta alta de color azul."),
    ALTA_AMARILLO("Tengo una carta alta de color amarillo."),
    ALTA_VERDE("Tengo una carta alta de color verde.");

    private final String mensaje;

    Mensajes(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean esDeJuego() {
        String n = this.name();
        return n.startsWith("BAJA") || n.startsWith("ALTA") || n.startsWith("INTERMEDIA");
    }
}
