package com.ipeirotis.service;

import com.ipeirotis.dao.QuestionDao;
import com.ipeirotis.dao.SurveyDao;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.entity.Answer;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Selection;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.entity.enums.AnswerType;
import com.ipeirotis.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyDao surveyDao;
    @Mock
    private QuestionDao questionDao;
    @Mock
    private UserAnswerService userAnswerService;

    @InjectMocks
    private SurveyService surveyService;

    private Survey validSurvey;

    @BeforeEach
    void setUp() {
        validSurvey = new Survey();
        validSurvey.setId("test-survey");
        validSurvey.setTitle("Test Survey");
        validSurvey.setDescription("A test survey");
        validSurvey.setReward(0.05);
        validSurvey.setMaxAssignments(10);
        validSurvey.setHtmlQuestion("<html><body>Test</body></html>");
    }

    // --- Validation tests ---

    @Test
    void create_validSurveyWithHtmlQuestion_succeeds() {
        assertDoesNotThrow(() -> surveyService.create(validSurvey));
    }

    @Test
    void create_missingId_throwsValidationException() {
        validSurvey.setId(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("id is required"));
    }

    @Test
    void create_missingTitle_throwsValidationException() {
        validSurvey.setTitle(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("title is required"));
    }

    @Test
    void create_missingDescription_throwsValidationException() {
        validSurvey.setDescription(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("description is required"));
    }

    @Test
    void create_missingReward_throwsValidationException() {
        validSurvey.setReward(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("reward is required"));
    }

    @Test
    void create_missingMaxAssignments_throwsValidationException() {
        validSurvey.setMaxAssignments(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("maxAssignments is required"));
    }

    @Test
    void create_noQuestionsOrHtmlOrTemplate_throwsValidationException() {
        validSurvey.setHtmlQuestion(null);
        validSurvey.setTemplate(null);
        validSurvey.setQuestions(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("questions, htmlQuestions or template are required"));
    }

    @Test
    void create_questionWithoutContent_throwsValidationException() {
        validSurvey.setHtmlQuestion(null);
        Question q = new Question();
        q.setContent(null);
        Answer a = new Answer();
        a.setType(AnswerType.freetext);
        q.setAnswers(List.of(a));
        validSurvey.setQuestions(List.of(q));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("question 1 content is required"));
    }

    @Test
    void create_questionWithoutAnswers_throwsValidationException() {
        validSurvey.setHtmlQuestion(null);
        Question q = new Question();
        q.setContent("What is your age?");
        q.setAnswers(null);
        validSurvey.setQuestions(List.of(q));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("at least one answer is required"));
    }

    @Test
    void create_selectionAnswerWithoutSelections_throwsValidationException() {
        validSurvey.setHtmlQuestion(null);
        Question q = new Question();
        q.setContent("Gender?");
        Answer a = new Answer();
        a.setType(AnswerType.selection);
        a.setSelections(null);
        q.setAnswers(List.of(a));
        validSurvey.setQuestions(List.of(q));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("selections are required"));
    }

    @Test
    void create_selectionWithoutIdentifier_throwsValidationException() {
        validSurvey.setHtmlQuestion(null);
        Question q = new Question();
        q.setContent("Gender?");
        Selection sel = new Selection();
        sel.setIdentifier(null);
        Answer a = new Answer();
        a.setType(AnswerType.selection);
        a.setSelections(List.of(sel));
        q.setAnswers(List.of(a));
        validSurvey.setQuestions(List.of(q));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        assertTrue(ex.getMessage().contains("identifier is required"));
    }

    @Test
    void create_multipleErrors_joinsAllErrors() {
        validSurvey.setId(null);
        validSurvey.setTitle(null);
        validSurvey.setReward(null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> surveyService.create(validSurvey));
        String msg = ex.getMessage();
        assertTrue(msg.contains("id is required"));
        assertTrue(msg.contains("title is required"));
        assertTrue(msg.contains("reward is required"));
    }

    @Test
    void create_htmlQuestionMinifiesWhitespace() {
        validSurvey.setHtmlQuestion("<div>  \n  <p>\t  Hello  \n  </p>  \n  </div>");
        surveyService.create(validSurvey);
        String html = validSurvey.getHtmlQuestion();
        assertFalse(html.contains("\n"));
        assertFalse(html.contains("\t"));
    }

    // --- getDecadeKey tests (via reflection) ---

    @Test
    void getDecadeKey_1985_returns1980to1990() throws Exception {
        String result = invokeGetDecadeKey("1985");
        assertEquals("1980-1990", result);
    }

    @Test
    void getDecadeKey_1990_returns1990to2000() throws Exception {
        String result = invokeGetDecadeKey("1990");
        assertEquals("1990-2000", result);
    }

    @Test
    void getDecadeKey_2000_returns2000to2010() throws Exception {
        String result = invokeGetDecadeKey("2000");
        assertEquals("2000-2010", result);
    }

    @Test
    void getDecadeKey_1955_returns1950to1960() throws Exception {
        // Integer division: 1955/10 = 195, Math.round(195) = 195, 195*10 = 1950
        String result = invokeGetDecadeKey("1955");
        assertEquals("1950-1960", result);
    }

    // --- toPercentage tests (via reflection) ---

    @Test
    void toPercentage_convertsCountsToPercentages() throws Exception {
        Map<String, Map<String, Float>> data = new LinkedHashMap<>();
        Map<String, Float> period1 = new HashMap<>();
        period1.put("Male", 3f);
        period1.put("Female", 7f);
        data.put("day1", period1);

        invokeToPercentage(data);

        assertEquals(30f, data.get("day1").get("Male"), 0.01f);
        assertEquals(70f, data.get("day1").get("Female"), 0.01f);
    }

    @Test
    void toPercentage_zeroCounts_returnsZero() throws Exception {
        Map<String, Map<String, Float>> data = new LinkedHashMap<>();
        Map<String, Float> emptyPeriod = new HashMap<>();
        data.put("day1", emptyPeriod);

        invokeToPercentage(data);
        // No values to check, just verify no exception
        assertTrue(data.get("day1").isEmpty());
    }

    @Test
    void toPercentage_singleValue_returns100() throws Exception {
        Map<String, Map<String, Float>> data = new LinkedHashMap<>();
        Map<String, Float> period = new HashMap<>();
        period.put("Male", 5f);
        data.put("day1", period);

        invokeToPercentage(data);

        assertEquals(100f, data.get("day1").get("Male"), 0.01f);
    }

    // --- getData tests (via reflection) ---

    @Test
    void getData_aggregatesByCountry() throws Exception {
        Map<String, List<UserAnswer>> answers = new LinkedHashMap<>();
        List<UserAnswer> dayAnswers = new ArrayList<>();

        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Male")));
        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Male")));
        dayAnswers.add(createUserAnswer("IN", Map.of("gender", "Female")));

        answers.put("day1", dayAnswers);

        DemographicsSurveyAnswers result = invokeGetData(answers);

        Map<String, Float> countryData = result.getCountries().get("day1");
        assertNotNull(countryData);
        // 2 US out of 3 = 66.67%, 1 Others out of 3 = 33.33%
        assertEquals(66.67f, countryData.get("US"), 0.5f);
        assertEquals(33.33f, countryData.get("Others"), 0.5f);
    }

    @Test
    void getData_aggregatesByGender() throws Exception {
        Map<String, List<UserAnswer>> answers = new LinkedHashMap<>();
        List<UserAnswer> dayAnswers = new ArrayList<>();

        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Male")));
        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Female")));
        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Female")));
        dayAnswers.add(createUserAnswer("US", Map.of("gender", "Female")));

        answers.put("day1", dayAnswers);

        DemographicsSurveyAnswers result = invokeGetData(answers);

        Map<String, Float> genderData = result.getGender().get("day1");
        assertNotNull(genderData);
        assertEquals(25f, genderData.get("Male"), 0.01f);
        assertEquals(75f, genderData.get("Female"), 0.01f);
    }

    @Test
    void getData_binsYearOfBirthIntoDecades() throws Exception {
        Map<String, List<UserAnswer>> answers = new LinkedHashMap<>();
        List<UserAnswer> dayAnswers = new ArrayList<>();

        dayAnswers.add(createUserAnswer("US", Map.of("yearOfBirth", "1985")));
        dayAnswers.add(createUserAnswer("US", Map.of("yearOfBirth", "1982")));
        dayAnswers.add(createUserAnswer("US", Map.of("yearOfBirth", "1993")));

        answers.put("day1", dayAnswers);

        DemographicsSurveyAnswers result = invokeGetData(answers);

        Map<String, Float> yobData = result.getYearOfBirth().get("day1");
        assertNotNull(yobData);
        // 1985 -> 1980-1990, 1982 -> 1980-1990, 1993 -> 1990-2000
        assertTrue(yobData.containsKey("1980-1990"));
        assertTrue(yobData.containsKey("1990-2000"));
    }

    @Test
    void getData_emptyAnswers_returnsEmptyMaps() throws Exception {
        Map<String, List<UserAnswer>> answers = new LinkedHashMap<>();
        answers.put("day1", new ArrayList<>());

        DemographicsSurveyAnswers result = invokeGetData(answers);

        assertNotNull(result.getCountries());
        assertTrue(result.getCountries().get("day1").isEmpty());
    }

    @Test
    void getData_nullAnswersMap_skipsGracefully() throws Exception {
        Map<String, List<UserAnswer>> answers = new LinkedHashMap<>();
        UserAnswer ua = new UserAnswer();
        ua.setLocationCountry("US");
        ua.setAnswers(null);
        answers.put("day1", List.of(ua));

        DemographicsSurveyAnswers result = invokeGetData(answers);
        // Should not throw; gender map should be empty since answers is null
        assertTrue(result.getGender().get("day1").isEmpty());
    }

    // --- Helper methods ---

    private UserAnswer createUserAnswer(String country, Map<String, String> answers) {
        UserAnswer ua = new UserAnswer();
        ua.setLocationCountry(country);
        ua.setAnswers(new HashMap<>(answers));
        return ua;
    }

    private String invokeGetDecadeKey(String year) throws Exception {
        Method method = SurveyService.class.getDeclaredMethod("getDecadeKey", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(surveyService, year);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    private void invokeToPercentage(Map<String, Map<String, Float>> data) throws Exception {
        Method method = SurveyService.class.getDeclaredMethod("toPercentage", Map.class);
        method.setAccessible(true);
        method.invoke(surveyService, data);
    }

    @SuppressWarnings("unchecked")
    private DemographicsSurveyAnswers invokeGetData(Map<String, List<UserAnswer>> answers) throws Exception {
        Method method = SurveyService.class.getDeclaredMethod("getData", Map.class);
        method.setAccessible(true);
        try {
            return (DemographicsSurveyAnswers) method.invoke(surveyService, answers);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
