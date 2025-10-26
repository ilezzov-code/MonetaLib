package ru.ilezzov.moneta.lib.api.model;

public record Response(boolean success, String message) {

    public static Response ok(String msg) {
        return new Response(true, msg);
    }

    public static Response error(String msg) {
        return new Response(false, msg);
    }

    @Override
    public String toString() {
        return (success ? "✅ " : "❌ ") + message;
    }
}
