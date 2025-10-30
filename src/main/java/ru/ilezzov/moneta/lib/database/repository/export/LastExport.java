package ru.ilezzov.moneta.lib.database.repository.export;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final LastExport export = (LastExport) o;
        return id == export.id && Objects.equals(lastExportDate, export.lastExportDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
