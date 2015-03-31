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
import com.ipeirotis.dto.BirthSurveyAnswers;
import com.ipeirotis.dto.ByCountryAnswers;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.dto.GenderSurveyAnswers;
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
                    .replaceAll("\t", "")
                    /*.replaceAll("\"", "\\\\'")*/);
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

    public List<UserAnswer> listAnswers(String surveyId, String from, String to) throws ParseException {
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.setTime(df.parse(from));
        dateFrom.set(Calendar.HOUR_OF_DAY, 0);
        dateFrom.set(Calendar.MINUTE, 0);
        dateFrom.set(Calendar.SECOND, 0);

        Calendar dateTo = Calendar.getInstance();
        dateTo.setTime(df.parse(to));
        dateTo.set(Calendar.HOUR_OF_DAY, 23);
        dateTo.set(Calendar.MINUTE, 59);
        dateTo.set(Calendar.SECOND, 59);

        Map<String, Object> params = new HashMap<String, Object>();
        if(surveyId != null) {
            params.put("surveyId", surveyId);
        }
        params.put("date >=", dateFrom.getTime());
        params.put("date <=", dateTo.getTime());

        return userAnswerService.query(params);
    }

    public DemographicsSurveyAnswers getDemographicsAnswers(String from, String to) throws ParseException {
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        Map<Date, Map<String, Float>> byCountry = new HashMap<Date, Map<String,Float>>();
        Map<Date, Map<String, Float>> byBirthday = new HashMap<Date, Map<String,Float>>();
        Map<Date, Map<String, Float>> byGender = new HashMap<Date, Map<String,Float>>();
        Map<Date, Map<String, Float>> byMaritalStatus = new HashMap<Date, Map<String,Float>>();
        Map<Date, Map<String, Float>> byHouseholdSize = new HashMap<Date, Map<String,Float>>();
        Map<Date, Map<String, Float>> byHouseholdIncome = new HashMap<Date, Map<String,Float>>();
        Map<String, Set<String>> labels = new HashMap<String, Set<String>>();
        result.setByCountry(byCountry);
        result.setByBirthday(byBirthday);
        result.setByGender(byGender);
        result.setByMaritalStatus(byMaritalStatus);
        result.setByHouseholdSize(byHouseholdSize);
        result.setByHouseholdIncome(byHouseholdIncome);

        Map<Date, List<UserAnswer>> dailyMap = new HashMap<Date, List<UserAnswer>>();
        List<UserAnswer> answers = listAnswers("demographics", from, to);

        for (UserAnswer userAnswer : answers) {
            Calendar d = Calendar.getInstance();
            d.setTime(userAnswer.getDate());
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);

            if (dailyMap.containsKey(d.getTime())) {
                List<UserAnswer> list = dailyMap.get(d.getTime());
                list.add(userAnswer);
            } else {
                List<UserAnswer> newList = new ArrayList<UserAnswer>();
                newList.add(userAnswer);
                dailyMap.put(d.getTime(), newList);
            }
        }

        for(Map.Entry<Date, List<UserAnswer>> entry : dailyMap.entrySet()) {
            Date day = entry.getKey();

            Map<String, Float> byBirthdayMap = new HashMap<String, Float>();
            byBirthday.put(day, byBirthdayMap);
            
            Map<String, Float> byCountryMap = new HashMap<String, Float>();
            byCountry.put(day, byCountryMap);

            Map<String, Float> byGenderMap = new HashMap<String, Float>();
            byGender.put(day, byGenderMap);

            Map<String, Float> byMaritalStatusMap = new HashMap<String, Float>();
            byMaritalStatus.put(day, byMaritalStatusMap);

            Map<String, Float> byHouseholdSizeMap = new HashMap<String, Float>();
            byHouseholdSize.put(day, byHouseholdSizeMap);

            Map<String, Float> byHouseholdIncomeMap = new HashMap<String, Float>();
            byHouseholdIncome.put(day, byHouseholdIncomeMap);

            for(UserAnswer userAnswer : entry.getValue()) {
                incCountries(userAnswer.getLocationCountry(), byCountryMap, labels);
                incDecades("yearOfBirth", userAnswer.getAnswers(), byBirthdayMap, labels);
                inc("gender", userAnswer.getAnswers(), byGenderMap, labels);
                inc("maritalStatus", userAnswer.getAnswers(), byMaritalStatusMap, labels);
                inc("householdSize", userAnswer.getAnswers(), byHouseholdSizeMap, labels);
                inc("householdIncome", userAnswer.getAnswers(), byHouseholdIncomeMap, labels);
            }
        }
        
        toPercentage(byCountry);
        toPercentage(byBirthday);
        toPercentage(byGender);
        toPercentage(byMaritalStatus);
        toPercentage(byHouseholdSize);
        toPercentage(byHouseholdIncome);

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
    
    private void incCountries(String id, Map<String, Float> dst, Map<String, Set<String>> labels) {
        Float count = dst.get(id);
        dst.put(id, (count == null) ? 1f : count + 1f);
        addLabel("countries", id, labels);
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

    private void toPercentage(Map<Date, Map<String, Float>> dst) {
        for(Map.Entry<Date, Map<String, Float>> entry : dst.entrySet()) {
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

    public List<GenderSurveyAnswers> getGenderAnswers(String from, String to) throws ParseException {
        Map<Date, GenderSurveyAnswers> dailyMap = new HashMap<Date, GenderSurveyAnswers>();
        List<UserAnswer> answers = listAnswers("gender", from, to);
        for (UserAnswer userAnswer : answers) {
            Calendar d = Calendar.getInstance();
            d.setTime(userAnswer.getDate());
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);

            GenderSurveyAnswers existing = dailyMap.get(d.getTime());
            if (existing != null) {
                if("male".equals(userAnswer.getAnswer())) {
                    existing.setMale(existing.getMale() + 1);
                } else if("female".equals(userAnswer.getAnswer())) {
                    existing.setFemale(existing.getFemale() + 1);
                }
            } else {
                existing = new GenderSurveyAnswers();
                existing.setDate(d.getTime());
                existing.setFemale(0);
                existing.setMale(0);

                if("male".equals(userAnswer.getAnswer())) {
                    existing.setMale(existing.getMale() + 1);
                } else if("female".equals(userAnswer.getAnswer())) {
                    existing.setFemale(existing.getFemale() + 1);
                }
                dailyMap.put(d.getTime(), existing);
            }
        }
        //to percentages
        for(GenderSurveyAnswers genderAnswers : dailyMap.values()) {
            float male = genderAnswers.getMale();
            float female = genderAnswers.getFemale();
            float malePercentage = (male+female == 0) ? 0 : male/(male+female) * 100;
            float femalePercentage = (male+female == 0) ? 0 : female/(male+female) * 100;
            genderAnswers.setMale(malePercentage);
            genderAnswers.setFemale(femalePercentage);
        }
        
        return new ArrayList<GenderSurveyAnswers>(dailyMap.values());
    }

    public BirthSurveyAnswers getBirthAnswers(String from, String to) throws ParseException {
        Set<String> decades = new TreeSet<String>();
        BirthSurveyAnswers result = new BirthSurveyAnswers();
        HashMap<Date, Map<String, Float>> dailyMap = new HashMap<Date, Map<String, Float>>();
        result.setData(dailyMap);
        result.setDecades(decades);

        List<UserAnswer> answers = listAnswers("birth", from, to);
        for (UserAnswer userAnswer : answers) {
            Calendar d = Calendar.getInstance();
            d.setTime(userAnswer.getDate());
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);
            
            Map<String, Float> byDecadeMap = dailyMap.get(d.getTime());
            if (byDecadeMap != null) {
                String key = getDecadeKey(userAnswer.getAnswer());
                decades.add(key);
                Float count = byDecadeMap.get(key);
                if(count != null) {
                    byDecadeMap.put(key, count + 1);
                } else {
                    byDecadeMap.put(key, 1f);
                }
            } else {
                byDecadeMap = new HashMap<String, Float>();
                String key = getDecadeKey(userAnswer.getAnswer());
                decades.add(key);
                byDecadeMap.put(key, 1f);

                dailyMap.put(d.getTime(), byDecadeMap);
            }
        }
        //to percentage
        for(Map.Entry<Date, Map<String, Float>> entry : dailyMap.entrySet()) {
            Float amount = 0f;
            Map<String, Float> map = entry.getValue();
            for(Float value : map.values()) {
                amount += value;
            }
            for(Map.Entry<String, Float> e : map.entrySet()) {
                map.put(e.getKey(), (amount == 0) ? 0 : e.getValue()/amount*100);
            }
        }
        return result;
    }

    public ByCountryAnswers getByCountryAnswers(String from, String to) throws ParseException {
        Set<String> countries = new TreeSet<String>();
        ByCountryAnswers result = new ByCountryAnswers();
        HashMap<Date, Map<String, Float>> dailyMap = new HashMap<Date, Map<String, Float>>();
        result.setData(dailyMap);
        result.setCountries(countries);

        List<UserAnswer> answers = listAnswers(null, from, to);
        for (UserAnswer userAnswer : answers) {
            Calendar d = Calendar.getInstance();
            d.setTime(userAnswer.getDate());
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);
            
            Map<String, Float> byCountryMap = dailyMap.get(d.getTime());
            if (byCountryMap != null) {
                String key = userAnswer.getLocationCountry();
                countries.add(key);
                Float count = byCountryMap.get(key);
                if(count != null) {
                    byCountryMap.put(key, count + 1);
                } else {
                    byCountryMap.put(key, 1f);
                }
            } else {
                byCountryMap = new HashMap<String, Float>();
                String key = userAnswer.getLocationCountry();
                countries.add(key);
                byCountryMap.put(key, 1f);

                dailyMap.put(d.getTime(), byCountryMap);
            }
        }
        //to percentage
        for(Map.Entry<Date, Map<String, Float>> entry : dailyMap.entrySet()) {
            Float amount = 0f;
            Map<String, Float> map = entry.getValue();
            for(Float value : map.values()) {
                amount += value;
            }
            for(Map.Entry<String, Float> e : map.entrySet()) {
                map.put(e.getKey(), (amount == 0) ? 0 : e.getValue()/amount*100);
            }
        }
        return result;
    }
}
