<template>
    <el-scrollbar height="100%" style="width: 100%;">
        <!-- 标题和搜索框 -->
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">借书证管理
            <el-input v-model="toSearch" :prefix-icon="Search"
                style=" width: 15vw;min-width: 150px; margin-left: 30px; margin-right: 30px; float: right;" clearable />
        </div>

        <!-- 借书证卡片显示区 -->
        <div style="display: flex;flex-wrap: wrap; justify-content: start;">

            <!-- 借书证卡片 -->
            <div class="cardBox" v-for="card in cards" v-show="card.name.includes(toSearch)" :key="card.cardId">
                <div>
                    <!-- 卡片标题 -->
                    <div style="font-size: 25px; font-weight: bold;">No. {{ card.cardId }}</div>

                    <el-divider />

                    <!-- 卡片内容 -->
                    <div style="margin-left: 10px; text-align: start; font-size: 16px;">
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">姓名：</span>{{ card.name }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">部门：</span>{{ card.department }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">类型：</span>{{ card.type }}</p>
                    </div>

                    <el-divider />

                    <!-- 卡片操作 -->
                    <div style="margin-top: 10px;">
                        <el-button type="primary" :icon="Edit" @click="this.toModifyInfo.cardId = card.cardId, this.toModifyInfo.name = card.name,
                this.toModifyInfo.department = card.department, this.toModifyInfo.type = card.type,
                this.modifyCardVisible = true" circle />
                        <el-button type="danger" :icon="Delete" circle
                            @click="this.toRemove = card.cardId, this.removeCardVisible = true"
                            style="margin-left: 30px;" />
                    </div>

                </div>
            </div>

            <!-- 新建借书证卡片 -->
            <el-button class="newCardBox"
                @click="newCardInfo.name = '', newCardInfo.department = '', newCardInfo.type = '学生', newCardVisible = true">
                <el-icon style="height: 50px; width: 50px;">
                    <Plus style="height: 100%; width: 100%;" />
                </el-icon>
            </el-button>

        </div>


        <!-- 新建借书证对话框 -->
        <el-dialog v-model="newCardVisible" title="新建借书证" width="30%" align-center>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                姓名：
                <el-input v-model="newCardInfo.name" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                部门：
                <el-input v-model="newCardInfo.department" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                类型：
                <el-select v-model="newCardInfo.type" size="middle" style="width: 12.5vw;">
                    <el-option v-for="type in types" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
            </div>

            <template #footer>
                <span>
                    <el-button @click="newCardVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmNewCard"
                        :disabled="newCardInfo.name.length === 0 || newCardInfo.department.length === 0">确定</el-button>
                </span>
            </template>
        </el-dialog>


        <!-- 修改信息对话框 -->   
        <el-dialog v-model="modifyCardVisible" :title="'修改信息(借书证ID: ' + this.toModifyInfo.cardId + ')'" width="30%"
            align-center>
          该功能并不完善，因为题目没要求
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                姓名：
                <el-input v-model="toModifyInfo.name" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                部门：
                <el-input v-model="toModifyInfo.department" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                类型：
                <el-select v-model="toModifyInfo.type" size="middle" style="width: 12.5vw;">
                    <el-option v-for="type in types" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="modifyCardVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmModifyCard"
                        :disabled="toModifyInfo.name.length === 0 || toModifyInfo.department.length === 0">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 删除借书证对话框 -->  
        <el-dialog v-model="removeCardVisible" title="删除借书证" width="30%">
            <span>确定删除<span style="font-weight: bold;">{{ toRemove }}号借书证</span>吗？</span>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="removeCardVisible = false">取消</el-button>
                    <el-button type="danger" @click="ConfirmRemoveCard">
                        删除
                    </el-button>
                </span>
            </template>
        </el-dialog>

    </el-scrollbar>
</template>

<script>
import { Delete, Edit, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
    data() {
        return {
            cards: [],
            Delete,
            Edit,
            Search,
            toSearch: '', // 搜索内容
            types: [ // 借书证类型
                {
                    value: '教师',
                    label: '教师',
                },
                {
                    value: '学生',
                    label: '学生',
                }
            ],
            newCardVisible: false, // 新建借书证对话框可见性
            removeCardVisible: false, // 删除借书证对话框可见性
            toRemove: 0, // 待删除借书证号
            newCardInfo: { // 待新建借书证信息
                name: '',
                department: '',
                type: '学生'
            },
            modifyCardVisible: false, // 修改信息对话框可见性
            toModifyInfo: { // 待修改借书证信息
                cardId: 0,
                name: '',
                department: '',
                type: '学生'
            },
            modifyok: false,
        }
    },
    methods: {
        ConfirmNewCard() {
            // 发出POST请求
          if(this.newCardInfo.type==="学生")this.newCardInfo.type='Student';
          else if(this.newCardInfo.type==="教师")this.newCardInfo.type='Teacher';
            axios.post("/card",
                { // 请求体
                    name: this.newCardInfo.name,
                    department: this.newCardInfo.department,
                    type: this.newCardInfo.type
                })
                .then(response => {
                    ElMessage.success("借书证新建成功") // 显示消息提醒
                    this.newCardVisible = false // 将对话框设置为不可见
                    this.QueryCards() // 重新查询借书证以刷新页面
                }).catch(error => {
              if (error.response) {
                ElMessage.error('错误：'+ error.response.data);
              } else {
                ElMessage.error('请求失败，但没有收到响应：' + error.message);
              }
            })
        },
        async ConfirmModifyCard() {
            this.modifyok=true;
            await axios.delete("/card", {data: {cardId:this.toModifyInfo.cardId}})
                .catch(error => {
                  this.modifyok=false;
              if (error.response) {
                this.modifyok=false;
                ElMessage.error('错误：'+ error.response.data);
              } else {
                this.modifyok=false;
                ElMessage.error('请求失败，但没有收到响应：' + error.message);
              }
            });
           if(this.modifyok===false)return;
            if(this.toModifyInfo.type==="学生")this.toModifyInfo.type='Student';
            if(this.toModifyInfo.type==="教师")this.toModifyInfo.type='Teacher';
            axios.post("/card",
                {
                    cardId:this.toModifyInfo.cardId,
                    name:this.toModifyInfo.name,
                    department:this.toModifyInfo.department,
                    type:this.toModifyInfo.type
                })
                .then(response => {
                    ElMessage.success("借书证修改成功") // 显示消息提醒
                    this.modifyCardVisible = false // 将对话框设置为不可见
                    this.QueryCards() // 重新查询借书证以刷新页面
                }).catch(error => {
              if (error.response) {
                ElMessage.error('错误：'+ error.response.data);
              } else {
                ElMessage.error('请求失败，但没有收到响应：' + error.message);
              }
            })
                

        },
        ConfirmRemoveCard(){
          // console.log("????");
            axios.delete("/card",
                {data: {
                    cardId: this.toRemove
                  }})
                .then(response => {
                    ElMessage.success("借书证删除成功") // 显示消息提醒
                    this.removeCardVisible = false // 将对话框设置为不可见
                    this.QueryCards() // 重新查询借书证以刷新页面
                }).catch(error => {
              if (error.response) {
                ElMessage.error('错误：'+ error.response.data);
              } else {
                ElMessage.error('请求失败，但没有收到响应：' + error.message);
              }
            })
        },
        async QueryCards() {
            this.cards = [] // 清空列表
            let response = await axios.get('/card') // 向/card发出GET请求
                .then(response => {
                    let cards = response.data // 接收响应负载

                    cards.forEach(card => { // 对于每个借书证
                        if(card.type==="Student")card.type="学生";
                        if(card.type==="Teacher")card.type="教师";
                        this.cards.push(card); // 将其加入到列表中
                    })
                }).catch(error => {
                  if (error.response) {
                    ElMessage.error('错误：'+ error.response.data);
                  } else {
                    ElMessage.error('请求失败，但没有收到响应：' + error.message);
                  }
                })
        }
    },
    mounted() { // 当页面被渲染时
        this.QueryCards() // 查询借书证
    }
}

</script>


<style scoped>
.cardBox {
    height: 300px;
    width: 200px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
    margin-top: 40px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
}

.newCardBox {
    height: 300px;
    width: 200px;
    margin-top: 40px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
}
</style>