package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElectivePickerTest {

    @Test
    @DisplayName("an empty list still has one page, so the picker never divides by zero")
    void emptyListHasOnePage() {
        assertThat(ElectivePicker.totalPages(0)).isEqualTo(1);
        assertThat(ElectivePicker.clampPage(5, 0)).isZero();
    }

    @Test
    @DisplayName("pages are counted in eights")
    void countsPagesInEights() {
        assertThat(ElectivePicker.totalPages(8)).isEqualTo(1);
        assertThat(ElectivePicker.totalPages(9)).isEqualTo(2);
        assertThat(ElectivePicker.totalPages(17)).isEqualTo(3);
    }

    @Test
    @DisplayName("a page out of range is pulled back in, not rejected")
    void clampsPageIntoRange() {
        // The timetable can shrink under an open menu, taking its last page with it.
        assertThat(ElectivePicker.clampPage(9, 9)).isEqualTo(1);
        assertThat(ElectivePicker.clampPage(-3, 9)).isZero();
        assertThat(ElectivePicker.clampPage(1, 9)).isEqualTo(1);
    }
}
