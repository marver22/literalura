package com.marver.literalura.model;

public enum Idioma {
    ES("es"),
    EN("en"),
    FR("fr");

    private String idioma;

    Idioma(String idioma) {
        this.idioma = idioma;
    }

    public String getIdioma(){
        return this.idioma;
    }

    public static Idioma fromString(String text) {
        for (Idioma idioma : Idioma.values()) {
            if (idioma.idioma.equalsIgnoreCase(text)) {
                return idioma;
            }
        }
        throw new IllegalArgumentException("Sin coincidencia con " + text);
    }
}
