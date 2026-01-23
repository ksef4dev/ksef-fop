package io.alapierre.ksef.fop;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpoGenerationParams {
    
    @NotNull
    private UpoSchema schema;

    @Builder.Default
    private Language language = Language.PL;
}
