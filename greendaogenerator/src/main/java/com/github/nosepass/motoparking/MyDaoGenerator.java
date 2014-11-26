package com.github.nosepass.motoparking;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Generates the SQLite GreenDAO java files one time for use later.
 */
public class MyDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "com.github.nosepass.motoparking.db");
        addParkingSpot(schema);
        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void addParkingSpot(Schema schema) {
        Entity e = schema.addEntity("ParkingSpot");
        addLocalId(e);
        addServerId(e);
        e.addStringProperty("name"); // the label of the this spot
        e.addStringProperty("description"); // details of the spot, what side of the street etc
        e.addDoubleProperty("latitude");
        e.addDoubleProperty("longitude");
        e.addBooleanProperty("paid"); // is it a metered spot or not
        e.addIntProperty("spaces"); // the number of slots at this spot (spots at this spot lol)
    }

    private static void addLocalId(Entity e) {
        e.addLongProperty("localId").columnName("_id").primaryKey();
    }

    private static void addServerId(Entity e) {
        e.addLongProperty("id").columnName("server_id");
    }
}
