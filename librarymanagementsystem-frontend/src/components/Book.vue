<!-- TODO: YOUR CODE HERE -->
<template>
  <el-scrollbar height="100%" style="width: 100%; height: 100%; ">
    <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; margin-bottom: 20px ">图书管理
    </div>
    <!-- 查询按钮 -->
    <el-button style="margin-left: 35px" type="primary" @click="queryBookVisible = true">点击筛选搜索条件</el-button>

    <!-- 添加按钮 -->
    <el-button style="margin-left: 50px" class="newBookAdd"
               @click="newBookInfo.category = '', newBookInfo.title='', newBookInfo.press='', newBookInfo.publishYear='',newBookInfo.author='', newBookInfo.price = '',newBookInfo.stock= '',newBookVisible=true">
      添加图书
    </el-button>

    <el-button class="incBookAdd" @click=" incBookId = '', incBookStock='',incBookVisible=true">
      改变库存
    </el-button>

    <el-button class="incBookAdd" @click="InitRawModify">
      修改图书信息
    </el-button>

    <el-button class="removeBook" type="danger" @click="removeBookId='', removeBookVisible=true">
      删除图书
    </el-button>

    <el-button style="margin-left: 50px" class="borrowBook" @click="borrowBookId='',borrowCardId='',borrowBookVisible=true">
      借出图书
    </el-button>

    <el-button class="returnBook" @click="returnBookId='',returnCardId='',returnBookVisible=true">
      归还图书
    </el-button>

    <!-- 图书显示 -->
    <el-table :data="books"
              style="width: 100%; margin-left: 20px; margin-top: 30px; margin-right: 30px; max-width: 80vw;table-layout: auto">
      <el-table-column prop="bookId" label="图书ID" sortable/>
      <el-table-column prop="category" label="类别" sortable/>
      <el-table-column prop="title" label="书名" sortable/>
      <el-table-column prop="press" label="出版社" sortable/>
      <el-table-column prop="publishYear" label="出版年份" sortable/>
      <el-table-column prop="author" label="作者" sortable/>
      <el-table-column prop="price" label="价格" sortable/>
      <el-table-column prop="stock" label="库存" sortable/>
      <el-table-column label="操作"><template #default="{ row }">
          <el-button @click="modifyBook={...row},modifyBookVisible=true,rawModifyBookVisible=false">修改信息</el-button>
      </template>
      </el-table-column>

    </el-table>


    <!-- 添加图书对话框 -->
    <el-dialog v-model="newBookVisible" title="增加书本" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        种类：
        <el-input v-model="newBookInfo.category" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        书名：
        <el-input v-model="newBookInfo.title" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        出版社：
        <el-input v-model="newBookInfo.press" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        出版年份：
        <el-input v-model="newBookInfo.publishYear" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        作者：
        <el-input v-model="newBookInfo.author" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        价格：
        <el-input v-model="newBookInfo.price" style="width: 12.5vw;" placeholder="请输入大于0的数" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        库存：
        <el-input v-model="newBookInfo.stock" style="width: 12.5vw;" placeholder="请输入大于等于0的整数"
                  clearable></el-input>
      </div>

      <template #footer>
                <span>
                    <el-button @click="newBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmNewBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="incBookVisible" title="改变库存" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        书号：
        <el-input v-model="incBookId" style="width: 12.5vw;" placeholder="请输入大于0的整数" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        增加库存：
        <el-input v-model="incBookStock" style="width: 12.5vw;" placeholder="负数表示减少库存" clearable></el-input>
      </div>

      <template #footer>
                <span>
                    <el-button @click="incBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmIncBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="removeBookVisible" title="删除书本" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        ID：
        <el-input v-model="removeBookId" style="width: 12.5vw;" clearable></el-input>
      </div>

      <template #footer>
                <span>
                    <el-button @click="removeBookVisible = false">取消</el-button>
                    <el-button type="danger" @click="ConfirmRemoveBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="returnBookVisible" title="归还图书" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        图书编号：
        <el-input v-model="returnBookId" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        借书证编号：
        <el-input v-model="returnCardId" style="width: 12.5vw;" clearable></el-input>
      </div>
      <template #footer>
                <span>
                    <el-button @click="returnBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmReturnBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="borrowBookVisible" title="借出图书" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        图书编号：
        <el-input v-model="borrowBookId" style="width: 12.5vw;" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        借书证编号：
        <el-input v-model="borrowCardId" style="width: 12.5vw;" clearable></el-input>
      </div>
      <template #footer>
                <span>
                    <el-button @click="borrowBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmBorrowBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="queryBookVisible" title="筛选搜索条件" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        类别：
        <el-input v-model="SearchConditions.category" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        作者：
        <el-input v-model="SearchConditions.author" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        出版社：
        <el-input v-model="SearchConditions.press" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        书名：
        <el-input v-model="SearchConditions.title" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        最低价格：
        <el-input v-model="SearchConditions.minPrice" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        最高价格：
        <el-input v-model="SearchConditions.maxPrice" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        最低出版年份：
        <el-input v-model="SearchConditions.minPublishYear" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>
      </div>

      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        最高出版年份：
        <el-input v-model="SearchConditions.maxPublishYear" style="width: 12.5vw;" clearable
                  placeholder="留空则忽略该条件"></el-input>

        <div style="margin-left: 0; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
          排序方式：
          <el-select
              v-model="SearchConditions.sortBy"
              clearable
              placeholder = "排序元素"
              style="width: 120px;">
            <el-option
                v-for="item in optionby"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>

          <el-select
            v-model="SearchConditions.sortOrder"
            clearable
            placeholder = "排序方式"
            style="margin-left: 10px; width: 120px;">
          <el-option
              v-for="item in optionorder"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

        </div>
      </div>

      <template #footer>
                <span>
                    <el-button @click="InitCondition">清空</el-button>
                    <el-button @click="borrowBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmQueryBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="modifyBookVisible" title="修改图书信息" width='30%' align-center>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        书号：
        <el-input :disabled="!rawModifyBookVisible" v-model="modifyBook.bookId" style="width: 12.5vw;" placeholder="要修改的书的编号" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        类别：
        <el-input v-model="modifyBook.category" style="width: 12.5vw;" placeholder="留空表示不修改" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        书名：
        <el-input v-model="modifyBook.title" style="width: 12.5vw;" placeholder="留空表示不修改" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        出版社：
        <el-input v-model="modifyBook.press" style="width: 12.5vw;" placeholder="留空表示不修改" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        作者：
        <el-input v-model="modifyBook.author" style="width: 12.5vw;" placeholder="留空表示不修改" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        价格：
        <el-input v-model="modifyBook.price" style="width: 12.5vw;" placeholder="留空表示不修改" clearable></el-input>
      </div>
      <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
        库存：
        <el-input v-model="modifyBook.stock" disabled style="width: 12.5vw;" placeholder="此处无法修改库存" clearable></el-input>
      </div>
      <template #footer>
                <span>
                    <el-button @click="modifyBookVisible = false">取消</el-button>
                    <el-button type="primary" :disabled="modifyBook===null||modifyBook.bookId===null||modifyBook.bookId.length===0 "  @click="ConfirmModifyBook">确定</el-button>
                </span>
      </template>
    </el-dialog>

  </el-scrollbar>
</template>

<script>
import axios from 'axios';
import {ElMessage} from 'element-plus';
import {Delete, Edit, Search} from '@element-plus/icons-vue'

export default {
  data() {
    return {
      books: [],
      Search,
      toSearch: '',
      SearchConditions: {
        category: null,
        title: null,
        press: null,
        maxPublishYear: null,
        minPublishYear: null,
        author: null,
        maxPrice: null,
        minPrice: null,
        sortOrder: null,
        sortBy: null
      },
      modifyBook:null,
      optionorder: [
        {
          value: "ASC",
          label: "升序",
        },
        {
          value: "DESC",
          label: "降序",
        }
      ],
      optionby: [
        {
          value: "BOOK_ID",
          label: "书号"
        },
        {
          value: "CATEGORY",
          label: "类别"
        },
        {
          value: "TITLE",
          label: "书名"
        },
        {
          value: "PRESS",
          label: "出版社"
        },
        {
          value: "PUBLISH_YEAR",
          label: "出版年份"
        },
        {
          value: "AUTHOR",
          label: "作者"
        },
        {
          value: "PRICE",
          label: "价格"
        },
        {
          value: "STOCK",
          label: "库存"
        }

      ],
      removeBookId: "",
      removeBookVisible: false,
      returnBookVisible: false,
      borrowBookVisible: false,
      returnBookId: "",
      returnCardId: "",
      borrowBookId: "",
      borrowCardId: "",
      newBookInfo: {
        category: '',
        title: '',
        press: '',
        publishYear: '',
        author: '',
        price: '',
        stock: ''
      },
      newBookVisible: false,
      incBookVisible: false,
      queryCardVisible: false,
      queryBookVisible: false,
      modifyBookVisible: false,
      rawModifyBookVisible: false,
      incBookId: "",
      incBookStock: "",
    }
  },
  methods: {
    InitCondition() {
        this.SearchConditions.category = null,
        this.SearchConditions.title = null,
        this.SearchConditions.press = null,
        this.SearchConditions.maxPublishYear = null,
        this.SearchConditions.minPublishYear = null,
        this.SearchConditions.author = null,
        this.SearchConditions.maxPrice = null,
        this.SearchConditions.minPrice = null,
        this.SearchConditions.sortOrder = null,
        this.SearchConditions.sortBy = null;
        this.QueryBooks();
    },
    ConfirmNewBook() {
      axios.post("/book",
          {
            category: this.newBookInfo.category,
            title: this.newBookInfo.title,
            press: this.newBookInfo.press,
            author: this.newBookInfo.author,
            publishYear: this.newBookInfo.publishYear,
            price: parseFloat(this.newBookInfo.price),
            stock: parseInt(this.newBookInfo.stock)
          })
          .then(response => {
            ElMessage.success("书本添加成功") // 显示消息提醒
            this.newBookVisible = false // 将对话框设置为不可见
            this.QueryBooks() // 重新查询书本以刷新页面
          }).catch(error => {
            if (error.response) {
              ElMessage.error('错误：'+ error.response.data);
            } else {
              ElMessage.error('请求失败，但没有收到响应：' + error.message);
            }
      });

    },
    ConfirmRemoveBook() {
      axios.delete("/book",
          {
            data: {
              bookId: parseInt(this.removeBookId)
            }
          }
      )
          .then(response => {
            this.removeBookVisible = false;
            ElMessage.success("删除成功");
            this.QueryBooks();
          }).catch(error => {
        if (error.response) {
          ElMessage.error('错误：'+ error.response.data);
        } else {
          ElMessage.error('请求失败，但没有收到响应：' + error.message);
        }
      });
    },
    QueryBooks() {
      this.books = []
      let response = axios.post('/book/query',
          this.SearchConditions) // 向/book发出GET请求
          .then(response => {
            let books = response.data // 接收响应负载
            books.forEach(book => { // 对于每本书
              this.books.push(book) // 将其加入到列表中
            })
          }).catch(error => {
            if (error.response) {
              ElMessage.error('错误：'+ error.response.data);
            } else {
              ElMessage.error('请求失败，但没有收到响应：' + error.message);
            }
          });

    },
    ConfirmReturnBook() {
      axios.post("/book/return",
          {
            bookId: this.returnBookId,
            cardId: this.returnCardId,
            returnTime: (new Date()).getTime()
          })
          .then(response => {
            ElMessage.success("归还成功");
            this.returnBookVisible = false;
            this.QueryBooks();
          }).catch(error => {
        if (error.response) {
          ElMessage.error('错误：'+ error.response.data);
        } else {
          ElMessage.error('请求失败，但没有收到响应：' + error.message);
        }
      });;
    },
    ConfirmBorrowBook() {
      axios.post("/book/borrow",
          {
            bookId: this.borrowBookId,
            cardId: this.borrowCardId,
            returnTime: (new Date()).getTime()
          })
          .then(response => {
            ElMessage.success("借出成功");
            this.borrowBookVisible = false;
            this.QueryBooks();
          }).catch(error => {
        if (error.response) {
          ElMessage.error('错误：'+ error.response.data);
        } else {
          ElMessage.error('请求失败，但没有收到响应：' + error.message);
        }
      });;
    },
    ConfirmIncBook() {
      axios.put("/book/inc",
          {
            bookId: parseInt(this.incBookId),
            deltaStock: parseInt(this.incBookStock)
          })
          .then(response => {
            ElMessage.success("更新成功");
            this.incBookVisible = false;
            this.QueryBooks();
          }).catch(error => {
        if (error.response) {
          ElMessage.error('错误：'+ error.response.data);
        } else {
          ElMessage.error('请求失败，但没有收到响应：' + error.message);
        }
      });;
    },
    ConfirmQueryBook() {
      this.QueryBooks();
    },
    ConfirmModifyBook(){
      axios.put("/book", this.modifyBook)
          .then(response => {
            ElMessage.success("修改成功");
            this.rawModifyBookVisible=false;
            this.modifyBookVisible = false;
            this.QueryBooks();
          }).catch(error => {
        if (error.response) {
          ElMessage.error('错误：'+ error.response.data);
        } else {
          ElMessage.error('请求失败，但没有收到响应：' + error.message);
        }
      });
    },
    InitRawModify(){
      this.modifyBook={
        bookId: "",
      };
      this.rawModifyBookVisible=true;
      this.modifyBookVisible=true;
    }
  },
  mounted() {
    this.QueryBooks()
  },

}

</script>