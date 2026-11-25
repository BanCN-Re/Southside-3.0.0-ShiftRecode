package dev.southside.utils.render.jello;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.utils.IMinecraft;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL20;

public class ShaderUtil implements IMinecraft {
   @SerializedName("programId")
   public final int programId;
   private static final String DEFAULT_TEXT = "float continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n";

   public ShaderUtil(String text, String text2) {
      int n = GL20.glCreateProgram();

      try {
         String text3 = readInputStream(text);
         InputStream in = text3 == null
            ? ((Resource)mc.getResourceManager().getResource(Identifier.of(text)).get()).getInputStream()
            : new ByteArrayInputStream(text3.getBytes());
         GL20.glAttachShader(n, this.computeInt(in, 35632));
         int n2 = this.computeInt(((Resource)mc.getResourceManager().getResource(Identifier.of(text2)).get()).getInputStream(), 35633);
         GL20.glAttachShader(n, n2);
      } catch (IOException e) {
         Southside.LOGGER.error("Unexpected error in ShaderUtil", e);
      }

      drawQuads(n, "Shader failed to link!");
      this.programId = n;
   }

   public ShaderUtil(String text) {
      this(text, "shaders/vertex.vsh");
   }

   public ShaderUtil() {
      int n = GL20.glCreateProgram();
      int n2 = this.computeInt(
         new ByteArrayInputStream(
            "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(0, halfpixel.y) * r);\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, 0) * r);\n   }\n   gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 * 2.0 + 1.0), mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n"
               .getBytes()
         ),
         35633
      );
      GL20.glAttachShader(n, n2);
      drawQuads(n, "Shader failed to link!");
      this.programId = n;
   }

   private static String readInputStream(String text) {
      return switch (text) {
         case "jelloBlur" -> "#version 110\n\nuniform sampler2D DiffuseSampler;\n\nvarying vec2 texCoord;\nvarying vec2 oneTexel;\n\nuniform vec2 InSize;\n\nuniform vec2 BlurDir;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    float totalStrength = 0.0;\n    float totalAlpha = 0.0;\n    float totalSamples = 0.0;\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n        vec4 sampleValue = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);\n\n\t\t// Accumulate average alpha\n        totalAlpha = totalAlpha + sampleValue.a;\n        totalSamples = totalSamples + 1.0;\n\n\t\t// Accumulate smoothed blur\n        float strength = 1.0 - abs(r / Radius);\n        totalStrength = totalStrength + strength;\n        blurred = blurred + sampleValue;\n    }\n    gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);\n}\n";
         case "roundRect2" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = continuousRectDistance(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha * alpha);\n}";
         case "gradientGlow" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius;\nuniform float soft;\n#define NOISE .5/255.0\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = continuousRectDistance(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius);\n    float smoothedAlpha = (1.0 - smoothstep(-soft, soft, distance));\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha);\n}";
         case "roundRect" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = continuousRectDistance(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(color1, smoothedAlpha * alpha);\n}";
         case "kawaseUpGlow" -> "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform bool check;\nuniform float lastPass;\nuniform float exposure;\n\nvoid main() {\n    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));\n}";
         case "glow" -> "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    innerColor *= weights[0];\n    for (float r = 1.0; r <= radius; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n";
         case "chams" -> "#version 120\n\nuniform sampler2D textureIn;\nuniform vec4 color;\nvoid main() {\n    float alpha = texture2D(textureIn, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(color.rgb, color.a * mix(0.0, alpha, step(0.0, alpha)));\n}\n";
         case "roundRectTexture" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\n\nvoid main() {\n    float distance = continuousRectDistance((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}";
         case "roundRectOutline" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\nvoid main() {\n    float distance = continuousRectDistance(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}";
         case "kawaseUpBloom" -> "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));\n}";
         case "outline" -> "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure;\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    for (float r = 1.0; r <= 1.0; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2);\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n";
         case "kawaseDownBloom" -> "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}";
         case "kawaseUp" -> "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(0, halfpixel.y) * r);\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, 0) * r);\n   }\n   gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 * 2.0 + 1.0), mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n";
         case "kawaseDown" -> "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum += texture2D(inTexture, uv);\n    gl_FragColor = vec4(sum.rgb * .5, 1.0);\n}\n";
         case "gradientMask" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\n#define radiusS 4.0\n#define NOISE .5/255.0\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(0.0019607843, -0.0019607843, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a * alpha;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);\n}";
         case "mask" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D u_texture, u_texture2;\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;\n    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;\n    gl_FragColor = vec4(tex2Color, texColorAlpha);\n}";
         case "gradient" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n#define NOISE .5/255.0\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    gl_FragColor = createGradient(coords, color1, color2, color3, color4);\n}";
         case "roundedRect" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\n\nvoid main() {\n    vec2 rectHalf = rectSize * .5;\n    // Smooth the result (free antialiasing).\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, continuousRectDistance(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - 1., radius))) * color.a;\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n\n}";
         case "roundedRectGradient" -> "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat continuousRectDistance(vec2 center, vec2 halfSize, float radius) {\n    float span = clamp(radius * 1.25, 0.0, max(0.0, min(halfSize.x, halfSize.y)));\n    vec2 d = abs(center) - halfSize + span;\n    vec2 q = max(d, 0.0);\n    q *= q;\n    return sqrt(sqrt(dot(q, q))) + min(max(d.x, d.y), 0.0) - span;\n}\n\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n   // use the bottom leftColor as the alpha\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., continuousRectDistance(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}";
         default -> null;
      };
   }

   private static void drawQuads(int n, String text) {
      GL20.glLinkProgram(n);
      if (GL20.glGetProgrami(n, 35714) == 0) {
         throw new IllegalStateException(text);
      }
   }

   public int computeInt(InputStream in, int n) {
      int n2 = GL20.glCreateShader(n);
      GL20.glShaderSource(n2, readInputStream(in));
      GL20.glCompileShader(n2);
      if (GL20.glGetShaderi(n2, 35713) == 0) {
         Southside.LOGGER.error("Shader compile log: {}", GL20.glGetShaderInfoLog(n2, 4096));
         throw new IllegalStateException(String.format("Shader (%s) failed to compile!", n));
      } else {
         return n2;
      }
   }

   public static String readInputStream(InputStream in) {
      StringBuilder sb = new StringBuilder();

      try {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

         String text;
         while ((text = bufferedReader.readLine()) != null) {
            sb.append(text).append('\n');
         }
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in ShaderUtil", e);
      }

      return sb.toString();
   }

   public void useProgram() {
      GL20.glUseProgram(this.programId);
   }

   public void useProgramEx() {
      GL20.glUseProgram(0);
   }

   public int getStringWidth(String text) {
      return GL20.glGetUniformLocation(this.programId, text);
   }

   public void setFloat(String text, float... floats) {
      int stringWidth = this.getStringWidth(text);
      switch (floats.length) {
         case 1:
            GL20.glUniform1f(stringWidth, floats[0]);
            break;
         case 2:
            GL20.glUniform2f(stringWidth, floats[0], floats[1]);
            break;
         case 3:
            GL20.glUniform3f(stringWidth, floats[0], floats[1], floats[2]);
            break;
         case 4:
            GL20.glUniform4f(stringWidth, floats[0], floats[1], floats[2], floats[3]);
      }
   }

   public void setInt(String text, int... values) {
      int stringWidth = this.getStringWidth(text);
      if (values.length > 1) {
         GL20.glUniform2i(stringWidth, values[0], values[1]);
      } else {
         GL20.glUniform1i(stringWidth, values[0]);
      }
   }

   public static void drawQuads(float x, float y, float width, float height) {
      GL20.glBegin(7);
      GL20.glTexCoord2f(0.0F, 0.0F);
      GL20.glVertex2f(x, y);
      GL20.glTexCoord2f(0.0F, 1.0F);
      GL20.glVertex2f(x, y + height);
      GL20.glTexCoord2f(1.0F, 1.0F);
      GL20.glVertex2f(x + width, y + height);
      GL20.glTexCoord2f(1.0F, 0.0F);
      GL20.glVertex2f(x + width, y);
      GL20.glEnd();
   }

   public static void drawQuads() {
      Window window = mc.getWindow();
      drawQuads(window.getScaledWidth(), window.getScaledHeight());
   }

   public static void drawQuads(float x, float y) {
      GL20.glBegin(7);
      GL20.glTexCoord2f(0.0F, 1.0F);
      GL20.glVertex2f(0.0F, 0.0F);
      GL20.glTexCoord2f(0.0F, 0.0F);
      GL20.glVertex2f(0.0F, y);
      GL20.glTexCoord2f(1.0F, 0.0F);
      GL20.glVertex2f(x, y);
      GL20.glTexCoord2f(1.0F, 1.0F);
      GL20.glVertex2f(x, 0.0F);
      GL20.glEnd();
   }
}
