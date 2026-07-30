package com.dbtraining.reconx.audit;

import org.springframework.context.ApplicationEventPublisher;

public class AuditEventPublisher {

    private final ApplicationEventPublisher publisher;
    private final AuditProperties props;

    public AuditEventPublisher(ApplicationEventPublisher publisher, AuditProperties props) {
        this.publisher = publisher;
        this.props = props;
    }

    public void publish(Object event) {
        publisher.publishEvent(event);
    }

    public String topic() {
        return props.getTopic();
    }
}
