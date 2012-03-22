package org.opencluster;

import org.junit.Assert;
import org.junit.Test;
import org.opencluster.util.ProtocolCommand;
import org.opencluster.util.ProtocolHeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: Brian Gorrie
 * Date: 5/11/11
 * Time: 8:09 PM
 * <p/>
 * Example of connecting to server.
 */
public class ProtocolTest {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7777;


    @Test
    public void ConnectAndSendHello() {

        // now on my machine it is connecting to localhost:7777 due to how I am plugging virtual box into my network.
        // Here is the command I use to start ocd on the ubuntu virtual box instance
        // ocd -l $(ifconfig | grep "inet addr" | head -n 1 | sed 's/:/ /g' | awk '{print $3}'):7777 -vvv

        // Create a non-blocking socket channel
        SocketChannel sChannel = createConnection();

        ProtocolHeader header = new ProtocolHeader(ProtocolCommand.HELLO);

        writeHeaderToConnection(sChannel, header);

        closeConnection(sChannel);

    }

    @Test
    public void ConnectAndSendHelloThenGoodBye() {

        // now on my machine it is connecting to localhost:7777 due to how I am plugging virtual box into my network.
        // Here is the command I use to start ocd on the ubuntu virtual box instance
        // ocd -l $(ifconfig | grep "inet addr" | head -n 1 | sed 's/:/ /g' | awk '{print $3}'):7777 -vvv

        // Create a non-blocking socket channel
        SocketChannel sChannel = createConnection();

        ProtocolHeader header = new ProtocolHeader(ProtocolCommand.HELLO);

        writeHeaderToConnection(sChannel, header);

        header = new ProtocolHeader(ProtocolCommand.GOODBYE);

        writeHeaderToConnection(sChannel, header);

        closeConnection(sChannel);

    }

    private void writeHeaderToConnection(SocketChannel sChannel, ProtocolHeader header) {
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        try {
            // Fill the buffer with the bytes to write;
            // see Putting Bytes into a ByteBuffer
            header.writeToByteBuffer(buf);

            // Prepare the buffer for reading by the socket
            buf.flip();

            // Write bytes
            int numBytesWritten = sChannel.write(buf);

            Assert.assertTrue("No data was written to socket channel.", numBytesWritten > 0);
            System.out.println("Bytes Written: " + numBytesWritten);

        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue("Exception occurred writing to socket: " + e.getMessage(),false);
        }
    }

    private void closeConnection(SocketChannel sChannel) {
        try {
            sChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue("Exception occurred closing socket: " + e.getMessage(), false);
        }
    }

    private SocketChannel createConnection() {
        SocketChannel sChannel = null;
        try {
            sChannel = SocketChannel.open();
            sChannel.configureBlocking(false);
            System.out.println("SO_LINGER: " + sChannel.getOption(StandardSocketOptions.SO_LINGER));

            // Send a connection request to the server; this method is non-blocking
            sChannel.connect(new InetSocketAddress(HOSTNAME, PORT));

            while (!sChannel.finishConnect()) {
                System.out.println("Not connected yet, waiting for connection to complete.");
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue("Exception occurred: " + e.getMessage(), false);
        }

        Assert.assertTrue("Unable to create connection through to server, double check it is running.", sChannel != null);
        return sChannel;
    }

}