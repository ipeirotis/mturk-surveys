package com.ipeirotis.service;

import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.MD5;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class UserAnswerServiceTest {

    @Test
    void applyPrivacyTransforms_hashesWorkerId() throws Exception {
        UserAnswerService service = new UserAnswerService();
        UserAnswer ua = new UserAnswer();
        ua.setWorkerId("WORKER123");
        ua.setIp("192.168.1.1");

        invokeApplyPrivacyTransforms(service, ua);

        assertEquals(MD5.crypt("WORKER123"), ua.getWorkerId());
        assertNull(ua.getIp());
    }

    @Test
    void applyPrivacyTransforms_nullWorkerId_staysNull() throws Exception {
        UserAnswerService service = new UserAnswerService();
        UserAnswer ua = new UserAnswer();
        ua.setWorkerId(null);
        ua.setIp("10.0.0.1");

        invokeApplyPrivacyTransforms(service, ua);

        assertNull(ua.getWorkerId());
        assertNull(ua.getIp());
    }

    @Test
    void applyPrivacyTransforms_stripsIpAddress() throws Exception {
        UserAnswerService service = new UserAnswerService();
        UserAnswer ua = new UserAnswer();
        ua.setWorkerId("W1");
        ua.setIp("192.168.1.1");

        invokeApplyPrivacyTransforms(service, ua);

        assertNull(ua.getIp());
    }

    @Test
    void applyPrivacyTransforms_consistentHashing() throws Exception {
        UserAnswerService service = new UserAnswerService();
        UserAnswer ua1 = new UserAnswer();
        ua1.setWorkerId("SAME_WORKER");
        UserAnswer ua2 = new UserAnswer();
        ua2.setWorkerId("SAME_WORKER");

        invokeApplyPrivacyTransforms(service, ua1);
        invokeApplyPrivacyTransforms(service, ua2);

        assertEquals(ua1.getWorkerId(), ua2.getWorkerId());
    }

    @Test
    void applyPrivacyTransforms_differentWorkers_differentHashes() throws Exception {
        UserAnswerService service = new UserAnswerService();
        UserAnswer ua1 = new UserAnswer();
        ua1.setWorkerId("WORKER_A");
        UserAnswer ua2 = new UserAnswer();
        ua2.setWorkerId("WORKER_B");

        invokeApplyPrivacyTransforms(service, ua1);
        invokeApplyPrivacyTransforms(service, ua2);

        assertNotEquals(ua1.getWorkerId(), ua2.getWorkerId());
    }

    private void invokeApplyPrivacyTransforms(UserAnswerService service, UserAnswer ua) throws Exception {
        Method method = UserAnswerService.class.getDeclaredMethod("applyPrivacyTransforms", UserAnswer.class);
        method.setAccessible(true);
        method.invoke(service, ua);
    }
}
