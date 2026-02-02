package app.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Clase utilitaria que proporciona animaciones reutilizables para la interfaz.
 * Utiliza las clases de animación de JavaFX (Timeline, KeyFrame, KeyValue).
 */
public class AnimacionUtils {

    /**
     * Aplica una animación de sacudida horizontal (shake) a un nodo.
     * Se usa típicamente para indicar errores en formularios.
     * El nodo al que se aplicará la animación (Button, TextField, etc.)
     */
    public static void shake(Node nodo) {
        // Guardamos la posición original del nodo
        double posicionOriginal = nodo.getTranslateX();

        // Creamos la timeline con los keyframes para el movimiento de sacudida
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(nodo.translateXProperty(), posicionOriginal)),
                new KeyFrame(Duration.millis(50), new KeyValue(nodo.translateXProperty(), posicionOriginal - 10)),
                new KeyFrame(Duration.millis(100), new KeyValue(nodo.translateXProperty(), posicionOriginal + 10)),
                new KeyFrame(Duration.millis(150), new KeyValue(nodo.translateXProperty(), posicionOriginal - 10)),
                new KeyFrame(Duration.millis(200), new KeyValue(nodo.translateXProperty(), posicionOriginal + 10)),
                new KeyFrame(Duration.millis(250), new KeyValue(nodo.translateXProperty(), posicionOriginal - 5)),
                new KeyFrame(Duration.millis(300), new KeyValue(nodo.translateXProperty(), posicionOriginal + 5)),
                new KeyFrame(Duration.millis(350), new KeyValue(nodo.translateXProperty(), posicionOriginal)));

        // Ejecutamos la animación
        timeline.play();
    }

    /**
     * Aplica una animación de sacudida más intensa para errores críticos.
     * El nodo al que se aplicará la animación
     */
    public static void shakeIntense(Node nodo) {
        double posicionOriginal = nodo.getTranslateX();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(nodo.translateXProperty(), posicionOriginal)),
                new KeyFrame(Duration.millis(40), new KeyValue(nodo.translateXProperty(), posicionOriginal - 15)),
                new KeyFrame(Duration.millis(80), new KeyValue(nodo.translateXProperty(), posicionOriginal + 15)),
                new KeyFrame(Duration.millis(120), new KeyValue(nodo.translateXProperty(), posicionOriginal - 15)),
                new KeyFrame(Duration.millis(160), new KeyValue(nodo.translateXProperty(), posicionOriginal + 15)),
                new KeyFrame(Duration.millis(200), new KeyValue(nodo.translateXProperty(), posicionOriginal - 10)),
                new KeyFrame(Duration.millis(240), new KeyValue(nodo.translateXProperty(), posicionOriginal + 10)),
                new KeyFrame(Duration.millis(280), new KeyValue(nodo.translateXProperty(), posicionOriginal - 5)),
                new KeyFrame(Duration.millis(320), new KeyValue(nodo.translateXProperty(), posicionOriginal + 5)),
                new KeyFrame(Duration.millis(360), new KeyValue(nodo.translateXProperty(), posicionOriginal)));

        timeline.play();
    }
}
