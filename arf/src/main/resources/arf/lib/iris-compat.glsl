// iris-compat.glsl - familiar Iris/OptiFine names mapped onto ARF uniforms (ARF API 1).
// This only cuts porting effort. Authors still rework block-ID logic, shadow distortion and buffer formats.
#include "arf/pbr.glsl"
#define gbufferModelView arf_View
#define gbufferProjection arf_Proj
#define sunPosition arf_SunDir
#define rainStrength arf_Rain
#define frameTimeCounter arf_GameTime
