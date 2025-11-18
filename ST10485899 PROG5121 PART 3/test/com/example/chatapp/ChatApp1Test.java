package com.example.chatapp;

import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.*;

public class ChatApp1Test {

    @Before
    public void setup() throws Exception {
        ChatApp1.contents.clear();
        ChatApp1.disregardedMessages.clear();
        ChatApp1.storedMessages.clear();
        ChatApp1.messageHashes.clear();
        ChatApp1.messageIDs.clear();

        Files.deleteIfExists(Paths.get("messages.json"));
        Files.deleteIfExists(Paths.get("stored_messages.json"));
    }

    // ---------------------------------------------------------
    // 1. ARRAY POPULATION TESTS
    // ---------------------------------------------------------

    @Test
    public void testArrayPopulation_SingleMessage() {
        ChatApp1.Message m = new ChatApp1.Message("ID1", "S", "+2783", "A", 0);
        ChatApp1.contents.add(m);
        ChatApp1.messageIDs.add("ID1");
        assertEquals(1, ChatApp1.contents.size());
    }

    @Test
    public void testArrayPopulation_MultipleMessages() {
        for (int i = 0; i < 5; i++) {
            ChatApp1.contents.add(new ChatApp1.Message("ID" + i, "S", "+2783", "M", i));
            ChatApp1.messageIDs.add("ID" + i);
        }
        assertEquals(5, ChatApp1.contents.size());
    }

    @Test
    public void testMessageHashStoredInArray() {
        ChatApp1.Message m = new ChatApp1.Message("X1", "S", "+2783", "HashTest", 0);
        ChatApp1.messageHashes.add(m.getMessageHash());
        assertTrue(ChatApp1.messageHashes.get(0).contains("X1"));
    }

    @Test
    public void testMessageIDStoredInArray() {
        ChatApp1.messageIDs.add("ZZ99");
        assertEquals("ZZ99", ChatApp1.messageIDs.get(0));
    }

    @Test
    public void testEmptyArraysStartEmpty() {
        assertEquals(0, ChatApp1.contents.size());
        assertEquals(0, ChatApp1.messageHashes.size());
        assertEquals(0, ChatApp1.messageIDs.size());
    }

    // ---------------------------------------------------------
    // 2. LONGEST MESSAGE TESTS
    // ---------------------------------------------------------

    @Test
    public void testLongestMessage_Basic() {
        ChatApp1.contents.add(new ChatApp1.Message("1", "S", "+27", "Short", 0));
        ChatApp1.contents.add(new ChatApp1.Message("2", "S", "+27", "This is longer", 1));

        String out = ChatApp1.displayLongestSentMessage();
        assertTrue(out.contains("This is longer"));
    }

    @Test
    public void testLongestMessage_WhenOnlyOne() {
        ChatApp1.contents.add(new ChatApp1.Message("A", "S", "+27", "Only one", 0));
        String out = ChatApp1.displayLongestSentMessage();
        assertTrue(out.contains("Only one"));
    }

    @Test
    public void testLongestMessage_WhenEmpty() {
        String out = ChatApp1.displayLongestSentMessage();
        assertTrue(out.contains("No"));
    }

    @Test
    public void testLongestMessage_TieLength() {
        ChatApp1.contents.add(new ChatApp1.Message("A", "S", "+27", "12345", 0));
        ChatApp1.contents.add(new ChatApp1.Message("B", "S", "+27", "ABCDE", 1));

        String out = ChatApp1.displayLongestSentMessage();
        assertTrue(out.contains("12345"));
    }

    // ---------------------------------------------------------
    // 3. SEARCH BY MESSAGE ID TESTS
    // ---------------------------------------------------------

    @Test
    public void testSearchByID_Found() {
        ChatApp1.Message m = new ChatApp1.Message("ABC", "S", "+2783", "Hi", 0);
        ChatApp1.contents.add(m);
        ChatApp1.messageIDs.add("ABC");

        String out = ChatApp1.searchByMessageID("ABC");
        assertTrue(out.contains("Hi"));
    }

    @Test
    public void testSearchByID_NotFound() {
        String out = ChatApp1.searchByMessageID("NOPE");
        assertTrue(out.contains("not found"));
    }

    @Test
    public void testSearchByID_EmptyString() {
        String out = ChatApp1.searchByMessageID("");
        assertTrue(out.contains("not found"));
    }

    @Test
    public void testSearchByID_MultipleMessages() {
        ChatApp1.contents.add(new ChatApp1.Message("ID1", "S", "+27", "A", 0));
        ChatApp1.contents.add(new ChatApp1.Message("ID2", "S", "+27", "B", 1));
        ChatApp1.messageIDs.add("ID2");

        String out = ChatApp1.searchByMessageID("ID2");
        assertTrue(out.contains("B"));
    }

    // ---------------------------------------------------------
    // 4. SEARCH BY RECIPIENT TESTS
    // ---------------------------------------------------------

    @Test
    public void testSearchByRecipient_FoundTwo() {
        ChatApp1.contents.add(new ChatApp1.Message("M1", "S", "+2783000", "MSG1", 0));
        ChatApp1.contents.add(new ChatApp1.Message("M2", "S", "+2783000", "MSG2", 1));

        String result = ChatApp1.searchByRecipient("+2783000");

        assertTrue(result.contains("MSG1"));
        assertTrue(result.contains("MSG2"));
    }

    /*@Test
    public void testSearchByRecipient_NoneFound() {
        String result = ChatApp1.searchByRecipient("+000");
        assertTrue(result.contains("No messages"));
    }*/

    @Test
    public void testSearchByRecipient_EmptyInput() {
        String result = ChatApp1.searchByRecipient("");
        assertTrue(result.contains("No"));
    }

    @Test
    public void testSearchByRecipient_DifferentRecipients() {
        ChatApp1.contents.add(new ChatApp1.Message("1", "S", "+111", "A", 0));
        ChatApp1.contents.add(new ChatApp1.Message("2", "S", "+222", "B", 1));

        String r = ChatApp1.searchByRecipient("+222");
        assertTrue(r.contains("B"));
    }

    // ---------------------------------------------------------
    // 5. DELETE BY HASH TESTS
    // ---------------------------------------------------------

    @Test
    public void testDeleteMessageByHash_Success() {
        ChatApp1.Message m = new ChatApp1.Message("D1", "S", "+27", "Delete", 0);
        ChatApp1.contents.add(m);
        ChatApp1.messageHashes.add(m.getMessageHash());

        boolean deleted = ChatApp1.deleteByMessageHash(m.getMessageHash());
        assertTrue(deleted);
    }

    @Test
    public void testDeleteMessageByHash_Fail() {
        boolean deleted = ChatApp1.deleteByMessageHash("BAD_HASH");
        assertFalse(deleted);
    }

    @Test
    public void testDeleteMessageByHash_EmptyArray() {
        boolean deleted = ChatApp1.deleteByMessageHash("ANY");
        assertFalse(deleted);
    }

    @Test
    public void testDeleteMessageByHash_RemovesCorrectIndex() {
        ChatApp1.Message m1 = new ChatApp1.Message("ID1", "S", "+27", "One", 0);
        ChatApp1.Message m2 = new ChatApp1.Message("ID2", "S", "+27", "Two", 1);

        ChatApp1.contents.add(m1);
        ChatApp1.contents.add(m2);

        ChatApp1.messageHashes.add(m1.getMessageHash());
        ChatApp1.messageHashes.add(m2.getMessageHash());

        ChatApp1.deleteByMessageHash(m1.getMessageHash());

        assertEquals("ID2", ChatApp1.contents.get(0).getMessageID());
    }

    // ---------------------------------------------------------
    // 6. JSON LOADING / FILE TESTS
    // ---------------------------------------------------------

    @Test
    public void testLoadStoredMessages_FileExists() throws Exception {
        String json = """
        [
          { "MessageID":"A1", "Sender":"S", "Recipient":"+2783", "Message":"Hello" }
        ]
        """;

        Files.writeString(Paths.get("stored_messages.json"), json);

        ChatApp1.loadStoredMessagesIntoArray();
        assertEquals(1, ChatApp1.storedMessages.size());
    }

    @Test
    public void testLoadStoredMessages_FileMissing() {
        ChatApp1.loadStoredMessagesIntoArray();
        assertEquals(0, ChatApp1.storedMessages.size());
    }

    @Test
    public void testMessageSavedToMessagesJson() throws Exception {
        ChatApp1.Message m = new ChatApp1.Message("X", "S", "+27", "SaveMe", 0);
        m.sentMessage(1);

        assertTrue(Files.exists(Paths.get("messages.json")));
    }

    @Test
    public void testStoreMessageCreatesStoredFile() throws Exception {
        ChatApp1.Message m = new ChatApp1.Message("X2", "S", "+27", "Draft", 1);
        m.sentMessage(2);

        assertTrue(Files.exists(Paths.get("stored_messages.json")));
    }

    // ---------------------------------------------------------
    // 7. REPORT TESTS
    // ---------------------------------------------------------

    @Test
    public void testDisplayReport_ShowsSender() {
        ChatApp1.contents.add(new ChatApp1.Message("R1", "SenderA", "+111", "Msg", 0));
        String out = ChatApp1.displayReportAllSentMessages();
        assertTrue(out.contains("SenderA"));
    }

    @Test
    public void testDisplayReport_ShowsAllFields() {
        ChatApp1.contents.add(new ChatApp1.Message("X", "S", "+99", "ABC", 0));
        String out = ChatApp1.displayReportAllSentMessages();

        assertTrue(out.contains("MessageID: X"));
        assertTrue(out.contains("Recipient: +99"));
        assertTrue(out.contains("Message: ABC"));
    }

    @Test
    public void testReportWhenNoMessages() {
        String out = ChatApp1.displayReportAllSentMessages();
        assertTrue(out.contains("No"));
    }
}




