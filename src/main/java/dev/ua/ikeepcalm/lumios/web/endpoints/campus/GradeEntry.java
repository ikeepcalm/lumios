package dev.ua.ikeepcalm.lumios.web.endpoints.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradeEntry {

    /** External user ID that was supplied at subscribe time (Telegram user ID). */
    private String id;

    private Long markId;

    private String disciplineName;

    /** Numeric grade value; null when only a presence mark was recorded. */
    private Double mark;

    /** Presence mark; empty when a numeric grade was recorded instead. */
    private String presence;

    /** Description of what the grade was awarded for (e.g. "Практичне заняття"). */
    private String description;

    private String employeeFullName;

}
