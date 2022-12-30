package microservices.book.multiplication.serviceclients;

import microservices.book.multiplication.challenge.ChallengeAttempt;
import microservices.book.multiplication.challenge.ChallengeSolvedEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChallengeEventPub {

    private final AmqpTemplate amqpTemplate;
    private final String challengeTopicExchange;

    public ChallengeEventPub(@Value("${ampq.exchange.attempts}") final String challengeTopicExchange, final AmqpTemplate amqpTemplate){
        this.amqpTemplate = amqpTemplate;
        this.challengeTopicExchange = challengeTopicExchange;
    }

    public void challengeSolved(final ChallengeAttempt challengeAttempt){
        ChallengeSolvedEvent event = buildEvent(challengeAttempt);
        // Routing Key is 'attempt.correct' or 'attempt.wrong'
        String routingKey = "attempt." + (event.isCorrect() ? "correct" : "wrong");
        amqpTemplate.convertAndSend(challengeTopicExchange, routingKey, event);
    }

    private ChallengeSolvedEvent buildEvent( final ChallengeAttempt challengeAttempt){
        return new ChallengeSolvedEvent(
                challengeAttempt.getId(),
                challengeAttempt.isCorrect(), challengeAttempt.getFactorA(),
                challengeAttempt.getFactorB(), challengeAttempt.getUser().getId(),
                challengeAttempt.getUser().getAlias());
    }
}
