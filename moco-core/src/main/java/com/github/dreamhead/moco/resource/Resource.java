package com.github.dreamhead.moco.resource;

import com.github.dreamhead.moco.ConfigApplier;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.Request;
import com.github.dreamhead.moco.model.MessageContent;
import com.google.common.base.Optional;

public class Resource implements Identifiable, ConfigApplier<Resource>, ResourceReader {
    private final Identifiable identifiable;
    private final ResourceConfigApplier configApplier;
    private final ResourceReader reader;

    public Resource(final Identifiable identifiable,
                    final ResourceConfigApplier configApplier,
                    final ResourceReader reader) {
        this.identifiable = identifiable;
        this.configApplier = configApplier;
        this.reader = reader;
    }

    @Override
    public Resource apply(final MocoConfig config) {
        return configApplier.apply(config, this);
    }

    @Override
    public String id() {
        return identifiable.id();
    }

    @Override
    public MessageContent readFor(final Optional<? extends Request> request) {
        return reader.readFor(request);
    }

    protected <T extends ResourceReader> T reader(final Class<T> clazz) {
        return clazz.cast(reader);
    }
}
