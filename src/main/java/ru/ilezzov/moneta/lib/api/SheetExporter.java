package ru.ilezzov.moneta.lib.api;

import ru.ilezzov.moneta.lib.api.model.Response;
import ru.ilezzov.moneta.lib.database.repository.export.LastExport;
import ru.ilezzov.moneta.lib.database.repository.export.LastExportRepository;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface SheetExporter {
    CompletableFuture<Response> exportData(final boolean addToCache);

    void clearSheet(final String... sheets);
}
