package com.agrim.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ServiceRoute — defines how gateway maps a path prefix to a backend service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRoute {

    private String serviceName;

    private String pathPrefix;

    private String targetBaseUrl;

    private boolean enabled;
}
