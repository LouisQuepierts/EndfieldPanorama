#version 330

uniform mat4 uProjectionViewMatrix;

layout(location = 0) in  vec3 Position;

out vec3 texCoord;

void main()
{
	texCoord = Position;
	gl_Position = uProjectionViewMatrix * vec4(Position, 1.0);
}