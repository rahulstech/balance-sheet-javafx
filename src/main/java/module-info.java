module rahulstech.jfx.balancesheet {
    requires com.google.gson;
    requires java.logging;
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.kordamp.ikonli.javafx;
    requires org.slf4j;
    requires ormlite.core;
    requires ormlite.jdbc;

    opens rahulstech.jfx.balancesheet.json.model to com.google.gson;

    opens rahulstech.jfx.balancesheet to javafx.graphics;
    opens rahulstech.jfx.balancesheet.controller to  javafx.fxml;

    opens rahulstech.jfx.balancesheet.database.entity to ormlite.core;
    opens rahulstech.jfx.balancesheet.database.internal to ormlite.core;
    opens rahulstech.jfx.balancesheet.database.model to ormlite.core;
    opens rahulstech.jfx.balancesheet.database.type to ormlite.core;

    exports rahulstech.jfx.balancesheet;
}