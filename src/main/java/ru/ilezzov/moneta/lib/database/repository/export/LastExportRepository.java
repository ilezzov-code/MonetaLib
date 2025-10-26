package ru.ilezzov.moneta.lib.database.repository.export;

import ru.ilezzov.moneta.lib.database.SQLDatabase;
import ru.ilezzov.moneta.lib.database.repository.DataRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LastExportRepository {
    private LastExport lastExport;
    private final SQLDatabase database;

    private final String SQL_SELECT = "SELECT * FROM last_export";
    private final String SQL_INSERT = "INSERT INTO last_export (export_date) VALUES (?) RETURNING id";
    private final String SQL_UPDATE = "UPDATE last_export SET export_date = ? WHERE id = ?";

    public LastExportRepository(final SQLDatabase database) {
        this.database = database;
    }

    public LastExport getLastExportDate() {
        if (lastExport == null) {
            try (final ResultSet resultSet = database.executeQuery(SQL_SELECT)) {
                if (resultSet.next()) {
                    final long id = resultSet.getLong(1);
                    final LocalDateTime localDateTime =  resultSet.getTimestamp("last_export_date").toLocalDateTime();

                    this.lastExport =  new LastExport(id, localDateTime);
                    return this.lastExport;
                }
                final LocalDateTime localDateTime = LocalDateTime.MIN;
                return insert(localDateTime);
            } catch (final SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return lastExport;
    }

    private LastExport insert(final LocalDateTime lastExportDate) {
        final Object[] args = new Object[]{lastExportDate};
        try (final ResultSet resultSet = database.executePreparedQuery(SQL_INSERT, args)) {

            if (resultSet.next()) {
                final long id = resultSet.getLong("id");
                this.lastExport = new LastExport(id, lastExportDate);
                return this.lastExport;
            };

            return null;
        } catch (final SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(final LocalDateTime date) {
        lastExport.setLastExportDate(date);
        save();
    }

    private void save() {
        try {
            final Object[] args = {
                    lastExport.getLastExportDate(),
                    lastExport.getId()
            };

            database.executePreparedUpdate(SQL_UPDATE, args);
        } catch (SQLException e) {

        }
    }

}
