package JSocket2.Protocol.EventHub;

public @interface OnEvent {
    public String value();
    public int priority() default 0;
}
