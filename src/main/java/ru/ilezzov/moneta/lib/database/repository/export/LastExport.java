package ru.ilezzov.moneta.lib.database.repository.export;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LastExport {
    private final long id;
    private LocalDateTime lastExportDate;

    public LastExport(final long id, final LocalDateTime lastExportDate) {
        this.id = id;
        this.lastExportDate = lastExportDate;
    }

    public LocalDateTime getLastExportDate() {
        return lastExportDate;
    }

    public void setLastExportDate(final LocalDateTime lastExportDate) {
        this.lastExportDate = lastExportDate;
    }

    public long getId() {
        return id;
    }
}
