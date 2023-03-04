package org.bot;

public class Transport {
    Integer number;
    String type;
    String gisType;

    Transport() {
        this.number = null;
        this.type = null;
        this.gisType = null;
    };

    Transport(Integer number, String type){
        this.number = number;
        this.type = type;
    }

    Transport(Integer number, String type, String gisType){
        this.number = number;
        this.type = type;
        this.gisType = gisType;
    }
}
