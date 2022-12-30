package microservices.book.multiplication.serviceclients;

import microservices.book.multiplication.challenge.ChallengeAttempt;
import microservices.book.multiplication.challenge.ChallengeSolvedEvent;
import microservices.book.multiplication.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(MockitoExtension.class)
class ChallengeEventPubTest {

    private ChallengeEventPub challengeEventPub;

    @Mock
    private AmqpTemplate amqpTemplate;

    @BeforeEach
    void setUp() {
        challengeEventPub = new ChallengeEventPub("test.topic", amqpTemplate);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void sentAttempt(boolean isCorrect){

        //given
        ChallengeAttempt attempt =  creatTestAttempt(isCorrect);

        //when
        challengeEventPub.challengeSolved(attempt);

        //then
        var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        var routingCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);
        verify(amqpTemplate).convertAndSend(exchangeCaptor.capture(), routingCaptor.capture(), eventCaptor.capture());
        then(exchangeCaptor.getValue()).isEqualTo("test.topic");
        then(routingCaptor.getValue()).isEqualTo("attempt." + (isCorrect ? "correct" : "wrong"));
        then(eventCaptor.getValue()).isEqualTo(solvedAttempt(isCorrect));
    }


    private ChallengeAttempt creatTestAttempt(boolean isCorrect) {
        return new ChallengeAttempt(1L, new User(10L, "John"),
                30, 40, isCorrect ? 1200 : 1300, isCorrect);
    }

    private ChallengeSolvedEvent solvedAttempt(boolean isCorrect) {
        return new ChallengeSolvedEvent(1L, isCorrect, 30, 40, 10L,
                "John");
    }
}