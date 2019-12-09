/**
 *
 */
package de.slux.line.friday.test.util;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.*;
import com.linecorp.bot.model.response.demographics.GetFriendsDemographicsResponse;
import com.linecorp.bot.model.richmenu.RichMenu;
import com.linecorp.bot.model.richmenu.RichMenuIdResponse;
import com.linecorp.bot.model.richmenu.RichMenuListResponse;
import com.linecorp.bot.model.richmenu.RichMenuResponse;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author slux
 */
public class LineMessagingClientMock implements LineMessagingClient {

    public static String KNOWN_USER_ID = "aaaaaaaa-bbbb-cccc-dddd-uuuuuuuuuuu0";
    public static String SPLIT_USER_ID = "aaaaaaaa-bbbb-cccc-dddd-uuuuuuuuuuu1";
    public static String GROUP_180_MEMBERS_ID = "aaaaaaaa-bbbb-cccc-dddd-ggggggggggg0";
    public static String GROUP_20_MEMBERS_ID = "aaaaaaaa-bbbb-cccc-dddd-ggggggggggg1";
    public static String GROUP_1_MEMBER_ID = "aaaaaaaa-bbbb-cccc-dddd-ggggggggggg2";

    private MessagingClientCallback callback;
    private List<Multicast> recordedMulticast;

    public LineMessagingClientMock(MessagingClientCallback callback) {
        this.callback = callback;
        this.recordedMulticast = new CopyOnWriteArrayList<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#replyMessage(com.linecorp.bot
     * .model.ReplyMessage)
     */
    @Override
    public CompletableFuture<BotApiResponse> replyMessage(ReplyMessage replyMessage) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#pushMessage(com.linecorp.bot.
     * model.PushMessage)
     */
    @Override
    public CompletableFuture<BotApiResponse> pushMessage(PushMessage pushMessage) {
        this.callback.pushMessageGenerated(pushMessage.toString());

        CompletableFuture<BotApiResponse> response = new CompletableFuture<>();
        response.complete(new BotApiResponse("", Collections.emptyList()));
        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#multicast(com.linecorp.bot.
     * model.Multicast)
     */
    @Override
    public CompletableFuture<BotApiResponse> multicast(Multicast multicast) {
        this.recordedMulticast.add(multicast);
        return CompletableFuture.completedFuture(new BotApiResponse("multicast " + multicast.getTo().size(), null));
    }

    @Override
    public CompletableFuture<BotApiResponse> broadcast(Broadcast broadcast) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getMessageContent(java.lang.
     * String)
     */
    @Override
    public CompletableFuture<MessageContentResponse> getMessageContent(String messageId) {

        return null;
    }

    @Override
    public CompletableFuture<MessageQuotaResponse> getMessageQuota() {
        return null;
    }

    @Override
    public CompletableFuture<QuotaConsumptionResponse> getMessageQuotaConsumption() {
        return null;
    }

    @Override
    public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentReplyMessages(String date) {
        return null;
    }

    @Override
    public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentPushMessages(String date) {
        return null;
    }

    @Override
    public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentMulticastMessages(String date) {
        return null;
    }

    @Override
    public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentBroadcastMessages(String date) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getProfile(java.lang.String)
     */
    @Override
    public CompletableFuture<UserProfileResponse> getProfile(String userId) {
        if (KNOWN_USER_ID.equals(userId)) {
            UserProfileResponse userProfile = new UserProfileResponse("slux", userId, null, "my status");
            return CompletableFuture.completedFuture(userProfile);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getGroupMemberProfile(java.
     * lang.String, java.lang.String)
     */
    @Override
    public CompletableFuture<UserProfileResponse> getGroupMemberProfile(String groupId, String userId) {
        return this.getProfile(userId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getRoomMemberProfile(java.
     * lang.String, java.lang.String)
     */
    @Override
    public CompletableFuture<UserProfileResponse> getRoomMemberProfile(String roomId, String userId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getGroupMembersIds(java.lang.
     * String, java.lang.String)
     */
    @Override
    public CompletableFuture<MembersIdsResponse> getGroupMembersIds(String groupId, String start) {
        if (GROUP_1_MEMBER_ID.equals(groupId)) {
            return CompletableFuture.completedFuture(new MembersIdsResponse(Arrays.asList(KNOWN_USER_ID), null));
        } else if (GROUP_20_MEMBERS_ID.equals(groupId)) {
            List<String> users = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                users.add(UUID.randomUUID().toString());
            }
            return CompletableFuture.completedFuture(new MembersIdsResponse(users, null));
        } else if (GROUP_180_MEMBERS_ID.equals(groupId)) {
            List<String> users = new ArrayList<>();

            for (int i = 0; i < (start == null ? 150 : 29); i++) {
                users.add(UUID.randomUUID().toString());
            }

            if (start == null) {
                return CompletableFuture.completedFuture(new MembersIdsResponse(users, SPLIT_USER_ID));
            } else {
                users.add(0, SPLIT_USER_ID);
                return CompletableFuture.completedFuture(new MembersIdsResponse(users, null));
            }
        } else {
            return CompletableFuture.completedFuture(new MembersIdsResponse(Collections.emptyList(), null));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getRoomMembersIds(java.lang.
     * String, java.lang.String)
     */
    @Override
    public CompletableFuture<MembersIdsResponse> getRoomMembersIds(String roomId, String start) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#leaveGroup(java.lang.String)
     */
    @Override
    public CompletableFuture<BotApiResponse> leaveGroup(String groupId) {

        CompletableFuture<BotApiResponse> future = new CompletableFuture<>();
        future.complete(new BotApiResponse("rosponse", null));
        return future;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#leaveRoom(java.lang.String)
     */
    @Override
    public CompletableFuture<BotApiResponse> leaveRoom(String roomId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getRichMenu(java.lang.String)
     */
    @Override
    public CompletableFuture<RichMenuResponse> getRichMenu(String richMenuId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#createRichMenu(com.linecorp.
     * bot.model.richmenu.RichMenu)
     */
    @Override
    public CompletableFuture<RichMenuIdResponse> createRichMenu(RichMenu richMenu) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#deleteRichMenu(java.lang.
     * String)
     */
    @Override
    public CompletableFuture<BotApiResponse> deleteRichMenu(String richMenuId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getRichMenuIdOfUser(java.lang
     * .String)
     */
    @Override
    public CompletableFuture<RichMenuIdResponse> getRichMenuIdOfUser(String userId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#linkRichMenuIdToUser(java.
     * lang.String, java.lang.String)
     */
    @Override
    public CompletableFuture<BotApiResponse> linkRichMenuIdToUser(String userId, String richMenuId) {

        return null;
    }

    @Override
    public CompletableFuture<BotApiResponse> linkRichMenuIdToUsers(List<String> userIds, String richMenuId) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#unlinkRichMenuIdFromUser(java
     * .lang.String)
     */
    @Override
    public CompletableFuture<BotApiResponse> unlinkRichMenuIdFromUser(String userId) {

        return null;
    }

    @Override
    public CompletableFuture<BotApiResponse> unlinkRichMenuIdFromUsers(List<String> userIds) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#getRichMenuImage(java.lang.
     * String)
     */
    @Override
    public CompletableFuture<MessageContentResponse> getRichMenuImage(String richMenuId) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.linecorp.bot.client.LineMessagingClient#setRichMenuImage(java.lang.
     * String, java.lang.String, byte[])
     */
    @Override
    public CompletableFuture<BotApiResponse> setRichMenuImage(String richMenuId, String contentType, byte[] content) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.linecorp.bot.client.LineMessagingClient#getRichMenuList()
     */
    @Override
    public CompletableFuture<RichMenuListResponse> getRichMenuList() {

        return null;
    }

    @Override
    public CompletableFuture<BotApiResponse> setDefaultRichMenu(String richMenuId) {
        return null;
    }

    @Override
    public CompletableFuture<RichMenuIdResponse> getDefaultRichMenuId() {
        return null;
    }

    @Override
    public CompletableFuture<BotApiResponse> cancelDefaultRichMenu() {
        return null;
    }

    @Override
    public CompletableFuture<IssueLinkTokenResponse> issueLinkToken(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<GetNumberOfMessageDeliveriesResponse> getNumberOfMessageDeliveries(String date) {
        return null;
    }

    @Override
    public CompletableFuture<GetNumberOfFollowersResponse> getNumberOfFollowersResponse(String date) {
        return null;
    }

    @Override
    public CompletableFuture<GetFriendsDemographicsResponse> getFriendsDemographics() {
        return null;
    }

    public List<Multicast> getRecordedMulticast() {
        return recordedMulticast;
    }

    public void cleanupMulticast() {
        this.recordedMulticast.clear();
    }
}
