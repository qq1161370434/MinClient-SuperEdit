package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.optifine.Config;
import net.optifine.shaders.SVertexBuilder;

import java.nio.ByteBuffer;
import java.util.List;

public class WorldVertexBufferUploader
{
    public void draw(BufferBuilder bufferBuilderIn)
    {
        if (bufferBuilderIn.getVertexCount() > 0)
        {
            if (bufferBuilderIn.getDrawMode() == 7 && Config.isQuadsToTriangles())
            {
                bufferBuilderIn.quadsToTriangles();
            }

            VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
            int i = vertexformat.getSize();
            ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            for (int j = 0; j < list.size(); ++j)
            {
                VertexFormatElement vertexformatelement = list.get(j);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();

                int k = vertexformatelement.getType().getGlConstant();
                int l = vertexformatelement.getIndex();
                bytebuffer.position(vertexformat.getOffset(j));

                switch (vertexformatelement$enumusage)
                {
                    case POSITION:
                        GlStateManager.glVertexPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                        GlStateManager.glEnableClientState(32884);
                        break;

                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + l);
                        GlStateManager.glTexCoordPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                        GlStateManager.glEnableClientState(32888);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;

                    case COLOR:
                        GlStateManager.glColorPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                        GlStateManager.glEnableClientState(32886);
                        break;

                    case NORMAL:
                        GlStateManager.glNormalPointer(k, i, bytebuffer);
                        GlStateManager.glEnableClientState(32885);
                }
            }

            if (bufferBuilderIn.isMultiTexture())
            {
                bufferBuilderIn.drawMultiTexture();
            }
            else if (Config.isShaders())
            {
                SVertexBuilder.drawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount(), bufferBuilderIn);
            }
            else
            {
                GlStateManager.glDrawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
            }

            int j1 = 0;

            for (int k1 = list.size(); j1 < k1; ++j1)
            {
                VertexFormatElement vertexformatelement1 = list.get(j1);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();

                int i1 = vertexformatelement1.getIndex();

                switch (vertexformatelement$enumusage1)
                {
                    case POSITION:
                        GlStateManager.glDisableClientState(32884);
                        break;

                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i1);
                        GlStateManager.glDisableClientState(32888);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;

                    case COLOR:
                        GlStateManager.glDisableClientState(32886);
                        GlStateManager.resetColor();
                        break;

                    case NORMAL:
                        GlStateManager.glDisableClientState(32885);
                }
            }
        }

        bufferBuilderIn.reset();
    }
}
