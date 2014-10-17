
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequesterStatistic.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RequesterStatistic">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NumberHITsAssignable"/>
 *     &lt;enumeration value="NumberHITsReviewable"/>
 *     &lt;enumeration value="NumberHITsCreated"/>
 *     &lt;enumeration value="NumberHITsCompleted"/>
 *     &lt;enumeration value="TotalRewardPayout"/>
 *     &lt;enumeration value="TotalRewardFeePayout"/>
 *     &lt;enumeration value="TotalFeePayout"/>
 *     &lt;enumeration value="TotalRewardAndFeePayout"/>
 *     &lt;enumeration value="TotalBonusPayout"/>
 *     &lt;enumeration value="TotalBonusFeePayout"/>
 *     &lt;enumeration value="EstimatedFeeLiability"/>
 *     &lt;enumeration value="EstimatedRewardLiability"/>
 *     &lt;enumeration value="EstimatedTotalLiability"/>
 *     &lt;enumeration value="NumberAssignmentsAvailable"/>
 *     &lt;enumeration value="NumberAssignmentsAccepted"/>
 *     &lt;enumeration value="NumberAssignmentsPending"/>
 *     &lt;enumeration value="NumberAssignmentsApproved"/>
 *     &lt;enumeration value="NumberAssignmentsRejected"/>
 *     &lt;enumeration value="NumberAssignmentsReturned"/>
 *     &lt;enumeration value="NumberAssignmentsAbandoned"/>
 *     &lt;enumeration value="AverageRewardAmount"/>
 *     &lt;enumeration value="PercentAssignmentsApproved"/>
 *     &lt;enumeration value="PercentAssignmentsRejected"/>
 *     &lt;enumeration value="NumberKnownAnswersCorrect"/>
 *     &lt;enumeration value="NumberKnownAnswersIncorrect"/>
 *     &lt;enumeration value="NumberKnownAnswersEvaluated"/>
 *     &lt;enumeration value="PercentKnownAnswersCorrect"/>
 *     &lt;enumeration value="NumberPluralityAnswersCorrect"/>
 *     &lt;enumeration value="NumberPluralityAnswersIncorrect"/>
 *     &lt;enumeration value="NumberPluralityAnswersEvaluated"/>
 *     &lt;enumeration value="PercentPluralityAnswersCorrect"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RequesterStatistic")
@XmlEnum
public enum RequesterStatistic {

    @XmlEnumValue("NumberHITsAssignable")
    NUMBER_HI_TS_ASSIGNABLE("NumberHITsAssignable"),
    @XmlEnumValue("NumberHITsReviewable")
    NUMBER_HI_TS_REVIEWABLE("NumberHITsReviewable"),
    @XmlEnumValue("NumberHITsCreated")
    NUMBER_HI_TS_CREATED("NumberHITsCreated"),
    @XmlEnumValue("NumberHITsCompleted")
    NUMBER_HI_TS_COMPLETED("NumberHITsCompleted"),
    @XmlEnumValue("TotalRewardPayout")
    TOTAL_REWARD_PAYOUT("TotalRewardPayout"),
    @XmlEnumValue("TotalRewardFeePayout")
    TOTAL_REWARD_FEE_PAYOUT("TotalRewardFeePayout"),
    @XmlEnumValue("TotalFeePayout")
    TOTAL_FEE_PAYOUT("TotalFeePayout"),
    @XmlEnumValue("TotalRewardAndFeePayout")
    TOTAL_REWARD_AND_FEE_PAYOUT("TotalRewardAndFeePayout"),
    @XmlEnumValue("TotalBonusPayout")
    TOTAL_BONUS_PAYOUT("TotalBonusPayout"),
    @XmlEnumValue("TotalBonusFeePayout")
    TOTAL_BONUS_FEE_PAYOUT("TotalBonusFeePayout"),
    @XmlEnumValue("EstimatedFeeLiability")
    ESTIMATED_FEE_LIABILITY("EstimatedFeeLiability"),
    @XmlEnumValue("EstimatedRewardLiability")
    ESTIMATED_REWARD_LIABILITY("EstimatedRewardLiability"),
    @XmlEnumValue("EstimatedTotalLiability")
    ESTIMATED_TOTAL_LIABILITY("EstimatedTotalLiability"),
    @XmlEnumValue("NumberAssignmentsAvailable")
    NUMBER_ASSIGNMENTS_AVAILABLE("NumberAssignmentsAvailable"),
    @XmlEnumValue("NumberAssignmentsAccepted")
    NUMBER_ASSIGNMENTS_ACCEPTED("NumberAssignmentsAccepted"),
    @XmlEnumValue("NumberAssignmentsPending")
    NUMBER_ASSIGNMENTS_PENDING("NumberAssignmentsPending"),
    @XmlEnumValue("NumberAssignmentsApproved")
    NUMBER_ASSIGNMENTS_APPROVED("NumberAssignmentsApproved"),
    @XmlEnumValue("NumberAssignmentsRejected")
    NUMBER_ASSIGNMENTS_REJECTED("NumberAssignmentsRejected"),
    @XmlEnumValue("NumberAssignmentsReturned")
    NUMBER_ASSIGNMENTS_RETURNED("NumberAssignmentsReturned"),
    @XmlEnumValue("NumberAssignmentsAbandoned")
    NUMBER_ASSIGNMENTS_ABANDONED("NumberAssignmentsAbandoned"),
    @XmlEnumValue("AverageRewardAmount")
    AVERAGE_REWARD_AMOUNT("AverageRewardAmount"),
    @XmlEnumValue("PercentAssignmentsApproved")
    PERCENT_ASSIGNMENTS_APPROVED("PercentAssignmentsApproved"),
    @XmlEnumValue("PercentAssignmentsRejected")
    PERCENT_ASSIGNMENTS_REJECTED("PercentAssignmentsRejected"),
    @XmlEnumValue("NumberKnownAnswersCorrect")
    NUMBER_KNOWN_ANSWERS_CORRECT("NumberKnownAnswersCorrect"),
    @XmlEnumValue("NumberKnownAnswersIncorrect")
    NUMBER_KNOWN_ANSWERS_INCORRECT("NumberKnownAnswersIncorrect"),
    @XmlEnumValue("NumberKnownAnswersEvaluated")
    NUMBER_KNOWN_ANSWERS_EVALUATED("NumberKnownAnswersEvaluated"),
    @XmlEnumValue("PercentKnownAnswersCorrect")
    PERCENT_KNOWN_ANSWERS_CORRECT("PercentKnownAnswersCorrect"),
    @XmlEnumValue("NumberPluralityAnswersCorrect")
    NUMBER_PLURALITY_ANSWERS_CORRECT("NumberPluralityAnswersCorrect"),
    @XmlEnumValue("NumberPluralityAnswersIncorrect")
    NUMBER_PLURALITY_ANSWERS_INCORRECT("NumberPluralityAnswersIncorrect"),
    @XmlEnumValue("NumberPluralityAnswersEvaluated")
    NUMBER_PLURALITY_ANSWERS_EVALUATED("NumberPluralityAnswersEvaluated"),
    @XmlEnumValue("PercentPluralityAnswersCorrect")
    PERCENT_PLURALITY_ANSWERS_CORRECT("PercentPluralityAnswersCorrect");
    private final String value;

    RequesterStatistic(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RequesterStatistic fromValue(String v) {
        for (RequesterStatistic c: RequesterStatistic.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
