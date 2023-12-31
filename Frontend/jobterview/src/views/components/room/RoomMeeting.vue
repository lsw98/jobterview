<template>
  <ReportModal
    v-if="displayModal"
    @close-modal-event="hideModal"
    :reporterNickname="nickname"
    :reportedNickname="reportNickname"
  ></ReportModal>
  <h2
    class="text-center mt-3"
    style="font-family: Arial, Helvetica, sans-serif"
  >
    {{ readRoomName }}
  </h2>
  <div class="container-wrapper mt-3">
    <div class="main-container">
      <div class="text-center">
        <div v-for="(user, index) in users" :key="user.id" class="text-center">
          <UserVideo
            v-if="index === 0"
            :info="user"
            :index="index"
            @changeMainVideo="changeMainVideo"
          ></UserVideo>
        </div>
        <!-- <UserVideo
          v-if="users[0]"
          :info="users[0]"
          :index="0"
          @changeMainVideo="changeMainVideo"
        ></UserVideo> -->
        <!-- <video
          ref="video"
          autoplay
          width="480"
          height="360"
          style="border-radius: 10%"
        ></video> -->
        <!-- <p class="text-center">{{ nickname }}</p> -->
        <p class="h2 text-center">
          <i
            v-if="camera"
            class="bi bi-camera-video-fill mx-3"
            @click="cameraClick"
          ></i>
          <i
            v-else
            class="bi bi-camera-video-off-fill mx-3"
            @click="cameraClick"
          ></i>
          <i v-if="mic" @click="muteClick" class="bi bi-mic-fill mx-3"></i>
          <i v-else @click="muteClick" class="bi bi-mic-mute-fill mx-3"></i>
          <i class="bi bi-box-arrow-right mx-3" @click="exitRoom"></i>
        </p>
        <!-- <button @click="debug">디버그 버튼</button> -->
      </div>
    </div>
    <!-- <div v-for="(user, index) in users" :key="user.id" class="text-center">
      <UserVideo
        v-if="index !== 0"
        :info="user"
        :index="index"
        @changeMainVideo="changeMainVideo"
      ></UserVideo>
    </div> -->
    <template v-for="(user, index) in users" :key="user.id">
      <div v-if="index !== 0" class="text-center">
        <UserVideo
          :info="user"
          :index="index"
          @changeMainVideo="changeMainVideo"
        >
        </UserVideo>
      </div>
    </template>
  </div>
  <div class="container-chatting" style="overflow-x: hidden">
    <div class="user-list">
      <p>
        <span>참여자 ({{ users.length }})</span>
      </p>
      <div class="user-nickname">
        <!-- <p>
          <i class="bi bi-person-fill"></i><span>{{ nickname }}</span
          ><i class="bi bi-exclamation-triangle-fill text-danger report"></i>
        </p> -->
        <p v-for="user in users" :key="user.id">
          <i class="bi bi-person-fill"></i><span>{{ user.nickname }}</span>
          <i
            class="bi bi-exclamation-triangle-fill text-danger report"
            @click="showModal(user.nickname)"
          ></i>
        </p>
      </div>
    </div>
    <div class="chatting mt-3">
      <p><span>실시간 채팅</span></p>
      <div class="chat-list">
        <div v-for="chat in chats" :key="chat.id">
          <p class="chat-nickname">{{ chat.nickname }} 님의 채팅</p>
          <p class="chat-content">{{ chat.content }}</p>
        </div>
      </div>
      <div class="row mt-4 chat-input">
        <div class="col-7">
          <input
            type="text"
            class="form-control"
            placeholder="채팅 입력..."
            v-model="chatContent"
            @keydown.enter.prevent="sendChat()"
          />
        </div>
        <div class="col-auto">
          <button type="submit" class="btn btn-secondary" @click="sendChat()">
            입력
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import UserVideo from "./UserVideo.vue";
import ReportModal from "../ReportModal";
import { mapState } from "vuex";
import router from "@/router";
import io from "socket.io-client";
import { socketUrl } from "@/config/config";

export default {
  name: "RoomMeeting",
  components: {
    UserVideo,
    ReportModal,
  },
  computed: {
    ...mapState("loginStore", ["loginNickname"]),
    ...mapState("roomStore", [
      "readRoomName",
      "readMaxMember",
      "readRoomPassword",
    ]),
  },
  async mounted() {
    this.enter_room();
    this.$socket = await io(socketUrl);
    this.$socket.on("all_users", async (allUsers) => {
      for (let i = 0; i < allUsers.length; i++) {
        //소켓id , nickname
        let pc = this.createPeerConnection(
          allUsers[i].id,
          allUsers[i].nickname
        ); //RTC객체 생성하고 배열에 담고 이벤트등록 및 트랙등록해서 리턴
        const offer = await pc.createOffer();
        pc.setLocalDescription(offer);
        this.$socket.emit("offer", {
          offer,
          offerSendID: this.$socket.id,
          offerReceiveID: allUsers[i].id,
          offerSendNickname: this.nickname,
        });
      }
    });

    this.$socket.on("getOffer", async (data) => {
      let pc = this.createPeerConnection(
        data.offerSendID,
        data.offerSendNickname
      );
      pc.setRemoteDescription(data.offer);
      const answer = await pc.createAnswer();
      pc.setLocalDescription(answer);
      this.$socket.emit("answer", {
        answer,
        answerSendID: this.$socket.id,
        answerReceiveID: data.offerSendID,
      });
    });
    this.$socket.on("getAnswer", (data) => {
      let pc = this.pcs[data.answerSendID];
      pc.setRemoteDescription(data.answer);
    });

    this.$socket.on("getCandidate", (data) => {
      let pc = this.pcs[data.candidateSendID];
      pc.addIceCandidate(data.candidate);
    });

    this.$socket.on("user_exit", (data) => {
      this.pcs[data.id].close();
      delete this.pcs[data.id];
      this.users = this.users.filter((user) => user.id !== data.id);
    });

    this.$socket.on("receive_message", async (chat) => {
      await this.chats.push(chat);
      this.autoScroll();
    });
  },
  async created() {},
  data: () => {
    return {
      myStream: {},
      nickname: "",
      mic: true,
      camera: true,
      roomName: "",
      roomPassword: "",
      users: [], // 참여자 객체 저장하는 배열
      chats: [], // 채팅 객체 저장하는 배열
      chatContent: "", // 채팅 내용
      pcs: {},
      maxNum: 0,
      displayModal: false,
      reportNickname: "",
    };
  },
  props: {
    msg: String,
  },
  methods: {
    debug() {
      for (let i in this.pcs) {
        console.log(this.pcs[i].getConfiguration());
      }
    },
    changeMainVideo(index) {
      const removedVideo = this.users.splice(index, 1)[0];
      this.user = this.users.unshift(removedVideo);
    },
    // this.createPeerConnection(allUsers[i].id,allUsers[i].nickname);
    createPeerConnection(socketID, nickname) {
      let pc = new RTCPeerConnection({
        iceServers: [
          {
            urls: "stun:i9a701.p.ssafy.io:3478",
          },
          {
            urls: "turn:i9a701.p.ssafy.io:3478",
            username: "ksc",
            credential: "036",
          },
        ],
      });
      this.pcs = { ...this.pcs, [socketID]: pc };

      pc.addEventListener("icecandidate", (data) =>
        this.handleIceCandidate(data, socketID)
      );
      pc.addEventListener("addstream", (data) => {
        this.handleAddStream(data, socketID, nickname);
      });
      this.myStream.getTracks().forEach((track) => {
        pc.addTrack(track, this.myStream);
      });

      return pc;
    },
    handleAddStream(data, socketID, nickname) {
      this.users.push({ id: socketID, nickname, stream: data.stream });
    },
    handleIceCandidate(data, socketID) {
      this.$socket.emit("candidate", {
        candidate: data.candidate,
        candidateSendID: this.$socket.id,
        candidateReceiveID: socketID,
      });
    },
    cameraClick() {
      this.myStream
        .getVideoTracks()
        .forEach((track) => (track.enabled = !track.enabled));
      this.camera = !this.camera;
    },
    muteClick() {
      this.myStream
        .getAudioTracks()
        .forEach((track) => (track.enabled = !track.enabled));
      this.mic = !this.mic;
    },

    //클릭하면 enter_room 실행된다.
    async enter_room() {
      await this.initCall(); //자신의 track이랑 video audio를 등록해놓고 시그널링 서버에 접속
      // this.$socket.emit("join_room", this.roomName);
    },
    async initCall() {
      await this.getMedia();
      this.nickname = this.loginNickname;
      this.roomName = this.readRoomName;
      this.maxNum = this.readMaxMember;
      this.roomPassword = this.readRoomPassword;
      this.$store.commit("roomStore/EMPTY_READ_ROOM_PASSWORD");
      this.$socket.emit("join_room", {
        //all user 시작하는거임 //offer도 저기서 완성시키고 보냄
        // store의 로그인 닉네임, url parameter의 roomNumber 받아오기
        nickname: this.nickname,
        roomName: this.roomName,
        maxNum: this.maxNum,
        roomPassword: this.roomPassword,
      });
    },
    async getMedia() {
      try {
        this.myStream = await navigator.mediaDevices.getUserMedia({
          audio: true,
          video: true,
        });
        this.users.push({
          id: this.$socket.id,
          nickname: this.loginNickname,
          stream: this.myStream,
        });
      } catch (e) {
        console.log(e);
      }
    },
    async sendChat() {
      let chat = {
        nickname: this.nickname,
        content: this.chatContent,
        roomName: this.roomName,
      };
      if (chat.content != "") {
        await this.chats.push(chat);
        this.$socket.emit("send_message", chat);
        this.chatContent = "";
      }
      this.autoScroll();
    },
    showModal(nickname) {
      this.reportNickname = nickname;
      this.displayModal = true;
    },
    hideModal() {
      this.displayModal = false;
    },
    exitRoom() {
      if (confirm("퇴장하시겠습니까?")) {
        router.push({ name: "RoomList" });
      }
    },
    autoScroll() {
      let scrollableDiv = document.querySelector(".chat-list");
      scrollableDiv.scrollTop = scrollableDiv.scrollHeight;
    },
  },
  beforeUnmount() {
    this.$socket.disconnect();
    if (this.myStream) {
      this.myStream.getTracks().forEach((track) => track.stop());
    }
  },
};
</script>

<style scoped>
.container-wrapper {
  display: grid;
  grid-template-columns: repeat(3, 400px);
  grid-template-rows: repeat(3, 240px);
  grid-template-areas:
    ". . . "
    "myCam myCam . "
    "myCam myCam . ";
}

.main-container {
  grid-area: myCam;
}

.container-chatting {
  left: 80%;
  top: 15%;
  position: absolute;
}

.user-list {
  background-color: #eaeaea;
  border-radius: 5px;
  width: 300px;
  height: 320px;
  right: 20px;
}

.user-list > div {
  background-color: #ffffff;
  border-radius: 5px;
  width: 250px;
  height: 245px;
  position: absolute;
  top: 50px;
  left: 25px;
}

.user-list > p {
  position: absolute;
  left: 25px;
  top: 15px;
}

.user-nickname p {
  position: relative;
  left: 10px;
  top: 10px;
}

.user-nickname span {
  position: relative;
  left: 10px;
}

.user-nickname .report {
  position: absolute;
  right: 40px;
}

.chatting {
  background-color: #eaeaea;
  border-radius: 5px;
  width: 300px;
  height: 370px;
  top: 30px;
  right: 20px;
}

.chat-list {
  background-color: #ffffff;
  border-radius: 5px;
  width: 250px;
  height: 250px;
  position: relative;
  top: 10px;
  left: 25px;
  overflow-x: hidden;
  overflow-y: auto;
}

.chatting > p {
  position: relative;
  left: 25px;
  top: 15px;
}

.chat-input {
  position: relative;
  left: 25px;
}

.chat-nickname {
  font-size: 8px;
  height: 8px;
  position: relative;
  left: 8px;
}

.chat-content {
  position: relative;
  left: 8px;
}

video {
  transform: rotateY(180deg);
  -webkit-transform: rotateY(180deg);
  /* Safari and Chrome */
  -moz-transform: rotateY(180deg);
  /* Firefox */
}
</style>
