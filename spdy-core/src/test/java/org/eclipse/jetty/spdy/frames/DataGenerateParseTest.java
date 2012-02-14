/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.spdy.frames;

import java.nio.ByteBuffer;

import org.eclipse.jetty.spdy.StandardCompressionFactory;
import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.StringDataInfo;
import org.eclipse.jetty.spdy.generator.Generator;
import org.eclipse.jetty.spdy.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

public class DataGenerateParseTest
{
    @Test
    public void testGenerateParse() throws Exception
    {
        testGenerateParse("test1");
    }

    @Test
    public void testGenerateParseZeroLength() throws Exception
    {
        testGenerateParse("");
    }

    private void testGenerateParse(String content) throws Exception
    {
        int length = content.length();
        DataInfo data = new StringDataInfo(content, true);
        int streamId = 13;
        Generator generator = new Generator(new StandardCompressionFactory().newCompressor());
        ByteBuffer buffer = generator.data(streamId, 2 * length, data);

        Assert.assertNotNull(buffer);

        TestSPDYParserListener listener = new TestSPDYParserListener();
        Parser parser = new Parser(new StandardCompressionFactory().newDecompressor());
        parser.addListener(listener);
        parser.parse(buffer);
        DataFrame frame2 = listener.getDataFrame();

        Assert.assertNotNull(frame2);
        Assert.assertEquals(streamId, frame2.getStreamId());
        Assert.assertEquals(DataInfo.FLAG_FIN, frame2.getFlags());
        Assert.assertEquals(length, frame2.getLength());
        Assert.assertEquals(length, listener.getData().remaining());
    }

    @Test
    public void testGenerateParseOneByteAtATime() throws Exception
    {
        String content = "test2";
        int length = content.length();
        DataInfo data = new StringDataInfo(content, true);
        int streamId = 13;
        Generator generator = new Generator(new StandardCompressionFactory().newCompressor());
        ByteBuffer buffer = generator.data(streamId, 2 * length, data);

        Assert.assertNotNull(buffer);

        TestSPDYParserListener listener = new TestSPDYParserListener();
        Parser parser = new Parser(new StandardCompressionFactory().newDecompressor());
        parser.addListener(listener);
        while (buffer.hasRemaining())
            parser.parse(ByteBuffer.wrap(new byte[]{buffer.get()}));
        DataFrame frame2 = listener.getDataFrame();

        Assert.assertNotNull(frame2);
        Assert.assertEquals(streamId, frame2.getStreamId());
        Assert.assertEquals(DataInfo.FLAG_FIN, frame2.getFlags());
        Assert.assertEquals(length, frame2.getLength());
        Assert.assertEquals(length, listener.getData().remaining());
    }
}
