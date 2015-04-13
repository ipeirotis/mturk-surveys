package com.ipeirotis.service;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.inject.Inject;
import com.ipeirotis.dao.QuestionDao;
import com.ipeirotis.dao.SurveyDao;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.entity.Answer;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Selection;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.entity.enums.AnswerType;
import com.ipeirotis.util.SafeDateFormat;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class SurveyService {

    private static final Logger logger = Logger.getLogger(SurveyService.class.getName());
    private static final String REGEX_WHITESPACE_BETWEEN_HTML = "[>]{1}\\s+[<]{1}";

    private static final String[] days = new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final Set<String> countryLabels = new LinkedHashSet<String>();
    private static final Set<String> incomeLabels = new LinkedHashSet<String>();

    static {
        countryLabels.add("US");
        countryLabels.add("India");
        countryLabels.add("Others");

        incomeLabels.add("Less than $10,000");
        incomeLabels.add("$10,000-$14,999");
        incomeLabels.add("$15,000-$24,999");
        incomeLabels.add("$25,000-$39,999");
        incomeLabels.add("$40,000-$59,999");
        incomeLabels.add("$60,000-$74,999");
        incomeLabels.add("$75,000-$99,999");
        incomeLabels.add("$100,000 or more");
    }

    private DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private UserAnswerService userAnswerService;

    @Inject
    public SurveyService(SurveyDao surveyDao, QuestionDao questionDao,
            UserAnswerService userAnswerService) {
        this.surveyDao = surveyDao;
        this.questionDao = questionDao;
        this.userAnswerService = userAnswerService;
    }

    public Survey create(Survey survey, Boolean production) throws BadRequestException {
        validate(survey);

        if(survey.getQuestions() != null && survey.getQuestions().size() > 0) {
            for(Question question : survey.getQuestions()) {
                question.setSurveyId(survey.getId());
            }
            questionDao.saveAll(survey.getQuestions());
        }
        if(survey.getTemplate() != null) {
            Map<String, String> model = new HashMap<String, String>();
            try {
                model.put("surveyId", URLEncoder.encode(survey.getId(), "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                model.put("surveyId", survey.getId());
            }
            if(production != null && production) {
                model.put("endpoint", "https://www.mturk.com");
            } else {
                model.put("endpoint", "https://workersandbox.mturk.com");
            }

            StringWriter writer = new StringWriter();
            Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(SurveyService.class, "/templates");
            Template template;
            try {
                template = cfg.getTemplate(survey.getTemplate());
                template.process(model, writer);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new BadRequestException(
                        String.format("Error reading template: %s", survey.getTemplate()));
            }

            survey.setHtmlQuestion(writer.toString());
        }
        if(survey.getHtmlQuestion() != null) {
            survey.setHtmlQuestion(survey.getHtmlQuestion()
                    .replaceAll(REGEX_WHITESPACE_BETWEEN_HTML, "><")
                    .replaceAll("\n", "")
                    .replaceAll("\t", ""));
        }
        
        return surveyDao.saveAndGet(survey);
    }

    public Survey get(String id) {
        Survey survey = surveyDao.get(id);
        if(survey != null) {
            List<Question> questions = questionDao.listByProperty("surveyId", id);
            if(questions != null && questions.size() > 0) {
                survey.setQuestions(questions);
            }
        }
        return survey;
    }

    public void delete(String id) throws NotFoundException {
        Survey survey = surveyDao.get(id);
        if(survey != null) {
            surveyDao.delete(survey);
            List<Question> questions = questionDao.listByProperty("surveyId", id);
            if(questions != null && questions.size() > 0) {
                questionDao.delete(questions);
            }
        } else {
            throw new NotFoundException(String.format("Survey with id=%s doesn't exist", id));
        }
    }

    private void validate(Survey survey) throws BadRequestException {
        List<String> errors = new ArrayList<String>();

        if(StringUtils.isBlank(survey.getId())) {
            errors.add("id is required");
        }

        if(StringUtils.isBlank(survey.getTitle())) {
            errors.add("title is required");
        }

        if(StringUtils.isBlank(survey.getDescription())) {
            errors.add("description is required");
        }

        if(survey.getReward() == null) {
            errors.add("reward is required");
        }
        
        if(survey.getMaxAssignments() == null) {
            errors.add("maxAssignments is required");
        }

        List<Question> questions = survey.getQuestions();
        if((questions == null || questions.size() == 0) && StringUtils.isBlank(survey.getHtmlQuestion()) &&
                StringUtils.isBlank(survey.getTemplate())) {
            errors.add("questions, htmlQuestions or template are required");
        } else if (questions != null) {
            int questionIndex = 1;
            for(Question question : questions) {
                if(StringUtils.isBlank(question.getContent())) {
                    errors.add(String.format("question %d content is required", questionIndex));
                }

                List<Answer> answers = question.getAnswers();
                if(answers == null || answers.size() == 0) {
                    errors.add(String.format("question %d at least one answer is required", questionIndex));
                } else {
                    int answerIndex = 1;
                    for(Answer answer : answers) {
                        if(answer.getType() == null) {
                            errors.add(String.format("question %d, answer %d type is required(freetext or selection)", questionIndex, answerIndex));
                        } else if (answer.getType() == AnswerType.selection) {
                            if (answer.getSelections() == null || answer.getSelections().size() == 0) {
                                   errors.add(String.format("question %d, answer %d selections are required because answer type is 'selection'", 
                                           questionIndex, answerIndex));
                            } else {
                                int selectionIndex = 1;
                                for(Selection selection : answer.getSelections()) {
                                    if(selection.getIdentifier() == null){
                                        errors.add(String.format("question %d, answer %d, selection %d identifier is required", 
                                                questionIndex, answerIndex, selectionIndex));
                                    }
                                    selectionIndex++;
                                }
                            }
                        }
                        answerIndex++;
                    }
                }
                questionIndex++;
            }
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(
                    String.format("Error saving survey: %s", StringUtils.join(errors, ", ")));
        }
    }

    public List<UserAnswer> listAnswers(String surveyId, Date from, Date to) {
        Map<String, Object> params = new HashMap<String, Object>();
        if(surveyId != null) {
            params.put("surveyId", surveyId);
        }
        params.put("date >=", from);
        params.put("date <", to);

        return userAnswerService.query(params);
    }

    public DemographicsSurveyAnswersByPeriod getDemographicsAnswers(String from, String to)
            throws ParseException {
        Map<String, List<UserAnswer>> hourlyMap = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> hourlyMapUS = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> hourlyMapIN = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dailyMap = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dailyMapUS = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dailyMapIN = new HashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dayOfWeekMap = new LinkedHashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dayOfWeekMapUS = new LinkedHashMap<String, List<UserAnswer>>();
        Map<String, List<UserAnswer>> dayOfWeekMapIN = new LinkedHashMap<String, List<UserAnswer>>();

        for(String day : days) {
            dayOfWeekMap.put(day, new ArrayList<UserAnswer>());
            dayOfWeekMapUS.put(day, new ArrayList<UserAnswer>());
            dayOfWeekMapIN.put(day, new ArrayList<UserAnswer>());
        }

        Calendar dateFrom = Calendar.getInstance();
        dateFrom.setTime(df.parse(from));
        dateFrom.set(Calendar.HOUR_OF_DAY, 0);
        dateFrom.set(Calendar.MINUTE, 0);
        dateFrom.set(Calendar.SECOND, 0);

        Calendar dateTo = Calendar.getInstance();
        dateTo.setTime(df.parse(to));
        dateTo.set(Calendar.HOUR_OF_DAY, 0);
        dateTo.set(Calendar.MINUTE, 0);
        dateTo.set(Calendar.SECOND, 0);
        dateTo.add(Calendar.DAY_OF_MONTH, 1);
        List<UserAnswer> answers = listAnswers("demographics", dateFrom.getTime(), dateTo.getTime());

        for (UserAnswer userAnswer : answers) {
            Calendar d = Calendar.getInstance();
            d.setTime(userAnswer.getDate());
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);

            Calendar dateWithHour = Calendar.getInstance();
            dateWithHour.setTime(userAnswer.getDate());
            dateWithHour.set(Calendar.MINUTE, 0);
            dateWithHour.set(Calendar.SECOND, 0);
            dateWithHour.set(Calendar.MILLISECOND, 0);

            String dayOfWeek = days[d.get(Calendar.DAY_OF_WEEK)-1];
            String hour = String.valueOf(dateWithHour.get(Calendar.HOUR_OF_DAY));

            aggregateAnswer(dailyMap, hourlyMap, dayOfWeekMap, userAnswer,
                    d.getTime().toString(), dayOfWeek, hour);

            if("US".equals(userAnswer.getLocationCountry())) {
                aggregateAnswer(dailyMapUS, hourlyMapUS, dayOfWeekMapUS, userAnswer,
                        d.getTime().toString(), dayOfWeek, hour);
            }

            if("IN".equals(userAnswer.getLocationCountry())) {
                aggregateAnswer(dailyMapIN, hourlyMapIN, dayOfWeekMapIN, userAnswer,
                        d.getTime().toString(), dayOfWeek, hour);
            }
        }

        DemographicsSurveyAnswersByPeriod result = new DemographicsSurveyAnswersByPeriod();
        Map<String, DemographicsSurveyAnswers> hourlyResult = new HashMap<String, DemographicsSurveyAnswers>();
        hourlyResult.put("all", getData(hourlyMap));
        hourlyResult.put("us", getData(hourlyMapUS));
        hourlyResult.put("in", getData(hourlyMapIN));

        Map<String, DemographicsSurveyAnswers> dailyResult = new HashMap<String, DemographicsSurveyAnswers>();
        dailyResult.put("all", getData(dailyMap));
        dailyResult.put("us", getData(dailyMapUS));
        dailyResult.put("in", getData(dailyMapIN));

        Map<String, DemographicsSurveyAnswers> dayOfWeekResult = new HashMap<String, DemographicsSurveyAnswers>();
        dayOfWeekResult.put("all", getData(dayOfWeekMap));
        dayOfWeekResult.put("us", getData(dayOfWeekMapUS));
        dayOfWeekResult.put("in", getData(dayOfWeekMapIN));
        result.setHourly(hourlyResult);
        result.setDaily(dailyResult);
        result.setWeekly(dayOfWeekResult);
        return result;
    }

    private void aggregateAnswer(Map<String, List<UserAnswer>> dailyMap, Map<String, List<UserAnswer>> hourlyMap,
            Map<String, List<UserAnswer>> dayOfWeekMap, UserAnswer userAnswer, String date, String dayOfWeek,
            String hour) {
        if(dailyMap.containsKey(date)) {
            List<UserAnswer> list = dailyMap.get(date);
            list.add(userAnswer);
        } else {
            List<UserAnswer> newList = new ArrayList<UserAnswer>();
            newList.add(userAnswer);
            dailyMap.put(date, newList);
        }

        dayOfWeekMap.get(dayOfWeek).add(userAnswer);

        if(hourlyMap.containsKey(hour)) {
            List<UserAnswer> list = hourlyMap.get(hour);
            list.add(userAnswer);
        } else {
            List<UserAnswer> newList = new ArrayList<UserAnswer>();
            newList.add(userAnswer);
            hourlyMap.put(hour, newList);
        }
    }

    private DemographicsSurveyAnswers getData(Map<String, List<UserAnswer>> answers){
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        Map<String, Map<String, Float>> byCountry = new LinkedHashMap<String, Map<String,Float>>();
        Map<String, Map<String, Float>> byYearOfBirth = new LinkedHashMap<String, Map<String,Float>>();
        Map<String, Map<String, Float>> byGender = new LinkedHashMap<String, Map<String,Float>>();
        Map<String, Map<String, Float>> byMaritalStatus = new LinkedHashMap<String, Map<String,Float>>();
        Map<String, Map<String, Float>> byHouseholdSize = new LinkedHashMap<String, Map<String,Float>>();
        Map<String, Map<String, Float>> byHouseholdIncome = new LinkedHashMap<String, Map<String,Float>>();

        Map<String, Set<String>> labels = new HashMap<String, Set<String>>();

        result.setCountries(byCountry);
        result.setYearOfBirth(byYearOfBirth);
        result.setGender(byGender);
        result.setMaritalStatus(byMaritalStatus);
        result.setHouseholdSize(byHouseholdSize);
        result.setHouseholdIncome(byHouseholdIncome);

        for(Map.Entry<String, List<UserAnswer>> entry : answers.entrySet()) {
            String key = entry.getKey();

            Map<String, Float> byYearOfBirthMap = new HashMap<String, Float>();
            byYearOfBirth.put(key, byYearOfBirthMap);
            
            Map<String, Float> byCountryMap = new HashMap<String, Float>();
            byCountry.put(key, byCountryMap);

            Map<String, Float> byGenderMap = new HashMap<String, Float>();
            byGender.put(key, byGenderMap);

            Map<String, Float> byMaritalStatusMap = new HashMap<String, Float>();
            byMaritalStatus.put(key, byMaritalStatusMap);

            Map<String, Float> byHouseholdSizeMap = new HashMap<String, Float>();
            byHouseholdSize.put(key, byHouseholdSizeMap);

            Map<String, Float> byHouseholdIncomeMap = new HashMap<String, Float>();
            byHouseholdIncome.put(key, byHouseholdIncomeMap);

            for(UserAnswer userAnswer : entry.getValue()) {
                incCountries(userAnswer.getLocationCountry(), byCountryMap, labels);
                incDecades("yearOfBirth", userAnswer.getAnswers(), byYearOfBirthMap, labels);
                inc("gender", userAnswer.getAnswers(), byGenderMap, labels);
                inc("maritalStatus", userAnswer.getAnswers(), byMaritalStatusMap, labels);
                inc("householdSize", userAnswer.getAnswers(), byHouseholdSizeMap, labels);
                inc("householdIncome", userAnswer.getAnswers(), byHouseholdIncomeMap, labels);
            }
        }

        toPercentage(byCountry);
        toPercentage(byYearOfBirth);
        toPercentage(byGender);
        toPercentage(byMaritalStatus);
        toPercentage(byHouseholdSize);
        toPercentage(byHouseholdIncome);

        //clear empty data
        if(labels.get("householdIncome") != null) {
            Iterator<String> incomeLabelsIterator = incomeLabels.iterator();
            while (incomeLabelsIterator.hasNext()) {
                String label = incomeLabelsIterator.next();
                if(!labels.get("householdIncome").contains(label)) {
                    incomeLabelsIterator.remove();
                }
            }
            labels.put("householdIncome", incomeLabels);
        }

        if(labels.get("countries") != null) {
            Set<String> existingCountries = new LinkedHashSet<String>();
            for(String label : countryLabels) {
                if(labels.get("countries").contains(label)) {
                    existingCountries.add(label);
                }
            }
            labels.put("countries", existingCountries);
        }

        result.setLabels(labels);
        return result;
    }

    private void inc(String questionId, Map<String, String> answers, Map<String, 
            Float> dst, Map<String, Set<String>> labels) {
        if(answers != null) {
            String answer = answers.get(questionId);
            
            if(answer != null) {
                Float count = dst.get(answer);
                dst.put(answer, (count == null) ? 1f : count + 1f);
                addLabel(questionId, answer, labels);
            }
        }
    }

    private void incDecades(String questionId, Map<String, String> answers, Map<String, Float> dst,
            Map<String, Set<String>> labels) {
        if(answers != null) {
            String answer = answers.get(questionId);
            if(answer != null) {
                String key = getDecadeKey(answer);
                Float count = dst.get(key);
                dst.put(key, (count == null) ? 1f : count + 1f);
                addLabel(questionId, key, labels);
            }
        }
    }
    
    private void incCountries(String code, Map<String, Float> dst, Map<String, Set<String>> labels) {
        String country;
        if("US".equals(code)) {
            country = "US";
        } else if("IN".equals(code)) {
            country = "India";
        } else {
            country = "Others";
        }
        Float count = dst.get(country);
        dst.put(country, (count == null) ? 1f : count + 1f);
        addLabel("countries", country, labels);
    }

    private void addLabel(String questionId, String answer, Map<String, Set<String>> labels) {
        Set<String> set = labels.get(questionId);
        if(set == null) {
            set = new TreeSet<String>();
            set.add(answer);
            labels.put(questionId, set);
        } else {
            set.add(answer);
        }
    }

    private void toPercentage(Map<String, Map<String, Float>> dst) {
        for(Map.Entry<String, Map<String, Float>> entry : dst.entrySet()) {
            Float amount = 0f;
            Map<String, Float> map = entry.getValue();
            for(Float value : map.values()) {
                amount += value;
            }
            for(Map.Entry<String, Float> e : map.entrySet()) {
                map.put(e.getKey(), (amount == 0) ? 0 : e.getValue()/amount*100);
            }
        }
    }

    private String getDecadeKey(String year) {
        Integer rounded = Math.round(Integer.parseInt(year)/10) * 10;
        return String.format("%d-%d", rounded, rounded+10);
    }

}
