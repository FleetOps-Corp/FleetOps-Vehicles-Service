package com.fleetops.vehicles.infrastructure.messaging.sqs.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsNotificationEnvelope {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("MessageAttributes")
    private Map<String, SnsMessageAttribute> messageAttributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, SnsMessageAttribute> getMessageAttributes() {
        return messageAttributes;
    }

    public void setMessageAttributes(Map<String, SnsMessageAttribute> messageAttributes) {
        this.messageAttributes = messageAttributes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SnsMessageAttribute {

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Value")
        private String value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
