package com.ipeirotis.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DatastoreRestoreServiceTest {

    @Test
    void resolveTable_null_returnsDefault() throws Exception {
        String result = invokeResolveTable(null);
        assertEquals("test.UserAnswer_2025MAR20", result);
    }

    @Test
    void resolveTable_empty_returnsDefault() throws Exception {
        String result = invokeResolveTable("");
        assertEquals("test.UserAnswer_2025MAR20", result);
    }

    @Test
    void resolveTable_blankSpaces_returnsDefault() throws Exception {
        String result = invokeResolveTable("   ");
        assertEquals("test.UserAnswer_2025MAR20", result);
    }

    @Test
    void resolveTable_qualifiedName_returnsAsIs() throws Exception {
        String result = invokeResolveTable("myDataset.myTable");
        assertEquals("myDataset.myTable", result);
    }

    @Test
    void resolveTable_unqualifiedName_prependsDefaultDataset() throws Exception {
        String result = invokeResolveTable("myTable");
        assertEquals("test.myTable", result);
    }

    @Test
    void resolveTable_tableWithDot_returnsAsIs() throws Exception {
        String result = invokeResolveTable("prod.responses");
        assertEquals("prod.responses", result);
    }

    @Test
    void surveyIdColumn_publicTable_usesSnakeCase() {
        assertEquals("survey_id", DatastoreRestoreService.surveyIdColumn("demographics.responses"));
    }

    @Test
    void surveyIdColumn_backupTable_usesEntityPropertyName() {
        assertEquals("surveyId", DatastoreRestoreService.surveyIdColumn("test.UserAnswer_2025MAR20"));
    }

    @Test
    void requireRestoreSource_publicTable_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatastoreRestoreService().requireRestoreSource("demographics.responses"));
    }

    @Test
    void requireRestoreSource_backupTables_areAccepted() {
        DatastoreRestoreService service = new DatastoreRestoreService();
        assertDoesNotThrow(() -> service.requireRestoreSource(null));
        assertDoesNotThrow(() -> service.requireRestoreSource("userAnswers_oct2020"));
    }

    private String invokeResolveTable(String table) throws Exception {
        DatastoreRestoreService service = new DatastoreRestoreService();
        Method method = DatastoreRestoreService.class.getDeclaredMethod("resolveTable", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, table);
    }
}
