module com.gossipuser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    requires MaterialFX;

    opens com.gossipuser to javafx.fxml;
    exports com.gossipuser;
}