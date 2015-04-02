#include <jni.h>
#include"processtxt.h"
#include"com_example_tagidentification_ResultsActivity.h"
#include <stdio.h>
#include <string.h>
#include<stdlib.h>
#include<ctype.h>


#define contentNum 48 //所有内容的个数
#define lineNum 50    //表格的行数
#define oneLineNum 3  //每行读取内容的个数
#define TRUE 1
#define FALSE 0


int processFile();
JNIEXPORT jint JNICALL Java_com_example_tagidentification_ResultsActivity_processFileNative
  (JNIEnv *, jobject){
	processFile();
	return 0;
}

//营养成分表结构体
struct fromContent{
    char *elementsName; //项目
    char *chineseUnit;  //中文单位
    char *englishUnit;  //英文单位
    float precision;    //精度
	float content;      //每100g或者100ml的含量
}fromContent[contentNum] ={
	"能量", "千焦", "kJ", 1.00, 0.00,
	"蛋白质", "克", "g", 0.10, 0.00,
	"脂肪", "克", "g", 0.10, 0.00,
	"饱和脂肪", "克", "g", 0.10, 0.00,
	"饱和脂肪酸", "克", "g", 0.10, 0.00,
	"反式脂肪", "克", "g", 0.10, 0.00,
	"反式脂肪酸", "克", "g", 0.10, 0.00,
	"单不饱和脂肪", "克", "g", 0.10, 0.00,
	"单不饱和脂肪酸", "克", "g", 0.10, 0.00,
	"多不饱和脂肪", "克", "g", 0.10, 0.00,
	"多不饱和脂肪酸", "克", "g", 0.10, 0.00,
	"胆固醇", "毫克", "mg", 1.00, 0.00,
	"碳水化合物", "克", "g", 0.10, 0.00,
	"糖", "克", "g", 0.10, 0.00,
	"乳糖c", "克", "g", 0.10, 0.00,
	"膳食纤维", "克", "g", 0.10, 0.00,
	"单体成分", "克", "g", 0.10, 0.00,
	"可溶性膳食纤维", "克", "g", 0.10, 0.00,
	"不可溶性膳食纤维", "克", "g", 0.10, 0.00,
	"钠", "毫克", "mg", 1.00, 0.00,
	"维生素A", "微克视黄醇当量", "μgRE", 1.00, 0.00,
	"维生素D", "微克", "μg", 0.10, 0.00,
	"维生素E", "毫克α-生育酚当量", "mgα-TE", 0.01, 0.00,
	"维生素K", "微克", "μg", 0.10, 0.00,
	"维生素B2", "毫克", "mg", 0.01, 0.00,
	"核黄素", "毫克", "mg", 0.01, 0.00,
	"维生素B6", "毫克", "mg", 0.01, 0.00,
	"维生素B12", "微克", "μg", 0.01, 0.00,
	"维生素C", "毫克", "mg", 0.10, 0.00,
	"抗坏血酸", "毫克", "mg", 0.10, 0.00,
	"烟酸", "毫克", "mg", 0.01, 0.00,
	"烟酰胺", "毫克", "mg", 0.01, 0.00,
	"叶酸", "微克", "μg", 1.00, 0.00,
	"叶酸", "微克叶酸当量", "μgDFE", 1.00, 0.00,
	"泛酸", "毫克", "mg", 0.01, 0.00,
	"生物素", "微克", "μg", 0.10, 0.00,
	"胆碱", "毫克", "mg", 0.10, 0.00,
	"磷", "毫克", "mg", 1.00, 0.00,
	"钾", "毫克", "mg", 1.00, 0.00,
	"镁", "毫克", "mg", 1.00, 0.00,
	"钙", "毫克", "mg", 1.00, 0.00,
	"铁", "毫克", "mg", 0.10, 0.00,
	"锌", "毫克", "mg", 0.01, 0.00,
	"碘", "微克", "μg", 0.10, 0.00,
	"硒", "微克", "μg", 0.10, 0.00,
	"铜", "毫克", "mg", 0.01, 0.00,
	"氟", "毫克", "mg", 0.01, 0.00,
	"锰", "毫克", "mg", 0.01, 0.00
};

unsigned int min_zhengge(unsigned int x,unsigned int y,unsigned int z );
unsigned int lev_distance(const char *s,const char *t);
float getContent(char *c);
void printfFromContent(void);

int processFile() {

	char line[lineNum]; //定义一个字符串数组，用于存储读取的字符
	//char lineTemp[lineNum] = "能置      2100千焦   25%     "; //定义一个字符串数组，用于存储读取的字符
	FILE *fFromContent = NULL; //定义文件流指针，用于打开读取的文件
	char *content[oneLineNum]; //定义每行中的内容
	char *contentTemp;         //用于临时保存content[oneLineNum]防止null的读入
	unsigned int distance = 0; //两个字符串之间的距离
	char correctContentNum = -1; //距离最小的ContentNum，即与content[0]最相似的fromContent[i].elementsName中的i
	char startContentNum = 0;    //每次循环开始的值，检测到一样的后，上面的不在检测

	char i = 0; //循环变量
	char d = 0; //用于报错调用lev_distance后得到的距离值，防止重复计算
	char energyFlag = FALSE; //是否为能量的标志位


//    fFromContent = fopen("mnt/sdcard/willdo.txt","r"); //读写方式打开文件fromContentV1.txt
    fFromContent = fopen("/data/data/com.example.tagidentification/files/result.txt","r");

	//逐行读取fFromContent所指向文件中的内容
	while (fgets(line, lineNum, fFromContent) != NULL)
	{
		//printf("%s", line);

		//将line中空格隔开的内容，分别存入content指针数组中
		contentTemp = strtok(line, " ");
		if(contentTemp)
			content[0] = contentTemp;

		for (i = 1; i < oneLineNum; i++){
			contentTemp = strtok(NULL, " ");
			if(contentTemp)
				content[i] = contentTemp;
		}

		//printf("%s %s\n", content[0], content[1]);
		//system("pause");

		//在fromContent中找到与content[0]最相似的
		distance = strlen(content[0]);

		for (i = startContentNum; i < contentNum; i++){

			//lev_distance == 0时，找到一样的
			if ((d = lev_distance(content[0], fromContent[i].elementsName)) == 0){
				correctContentNum = i;
				startContentNum = i + 1; //检测两个字符串一样时，将循环起始从下一行开始，不再检测之前的
				//到最后一个时，判断是否为 “能量”，
				if (correctContentNum == 0)
					energyFlag = TRUE;
				break; //若检查到完全一样的立即推出循环
			}
			//否则找到lev_distance值最小的，作为一致的
			else {
				//将距离最小的记录下来
				if (distance > d){
					distance = d;
					correctContentNum = i;
				}
				//到最后一个时，判断是否为 “能量”，
				if ((correctContentNum != -1) && (i == (contentNum-1))){
					if (correctContentNum == 0)
						energyFlag = TRUE;
				}
			}
		}

		//从能量开始输出
		if ((correctContentNum != -1) && (energyFlag == TRUE)){

			printf("%s->%s,distance = %d\n", content[0], fromContent[correctContentNum].elementsName, distance);
			printf("%.2f\n", getContent(content[1]));

			fromContent[correctContentNum].content = getContent(content[1]);
		}


    }
	fclose(fFromContent);

	printfFromContent();

	return 0;
}

//@func:将content[1]转化为float,即含量的大小
//@paras:c均为c风格字符串
float getContent(char *c)
{
	float content = 0;
	float power = 1;

	while (*c>='0' && *c<='9'){
		content = content * 10 + (*c - '0');
		c++;
	}
	if (*c == '.'){
		c++;
	}
	while (*c>='0' && *c<='9'){
		power *= 10;
		content = content * 10 + (*c - '0');
		c++;
	}

	//while (isdigit(*c)){
	//	content = content * 10 + (*c - '0');
	//	c++;
	//}
	//if (*c == '.'){
	//	c++;
	//}
	//while (isdigit(*c)){
	//	power *= 10;
	//	content = content * 10 + (*c - '0');
	//	c++;
	//}
	content /= power;
	//printf("%.2f\n", content);

	return content;
}

//@func:fromConten输出至txt文件中
//@paras:无
void printfFromContent(void)
{
	char i = 0;
	FILE *fDone; //定义文件流指针，用于打开读取的文件
//	fDone = fopen("mnt/sdcard/Done.txt","w"); //只读方式打开文件fromContentV1.txt
	fDone = fopen("/data/data/com.example.tagidentification/files/result.txt","w");

	for ( ; i < contentNum; i++){
		//printf("%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
		fprintf(fDone, "%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
	}
	fclose(fDone);
}


//@func:计算字符串s 和 t之间的levenshtein距离
//@paras:s和t均为c风格字符串
unsigned int lev_distance(const char *s,const char *t)
{
    //n:目标单词t的长度   m:源单词s的长度
    unsigned int m_tmp=0,n_tmp=0;
    unsigned int i;
	i=0;
	unsigned int j;



/*    const unsigned m=m_tmp+1;
    const unsigned n=n_tmp+1;
    unsigned matrix[m][n]; */

    //计算源单词长度
    while(s[i])
    {
        i++;
        m_tmp++;
    }
    //计算目标单词长度
    i=0;
    while(t[i])
    {
        i++;
        n_tmp++;
    }
    if(m_tmp==0)
        return n_tmp;
    if(n_tmp==0)
        return m_tmp;

	const unsigned int m=m_tmp+1;
    const unsigned int n=n_tmp+1;
    unsigned int **matrix;
	matrix = new unsigned int *[m];
	for(i=0;i<m;i++)
		matrix[i] = new unsigned int[n];

    //给矩阵的第0行和第0列赋值
    for(i=0;i<m;i++)
        matrix[i][0]=i;
    for(i=0;i<n;i++)
        matrix[0][i]=i;
    //填充矩阵的其他元素，逐行填充

    for(i=1;i<m;i++)
        for(j=1;j<n;j++)
        {
            unsigned int cost=1;
            if(s[i-1]==t[j-1])
                cost=0;
            matrix[i][j]=min_zhengge(matrix[i-1][j]+1,matrix[i][j-1]+1,matrix[i-1][j-1]+cost);
        }
    //查看矩阵各元素的值
//	for(i=0;i<m;i++)
//    {
//        for(j=0;j<n;j++)
//        {
//            printf("%d\t",matrix[i][j]);
//        }
//        printf("\n");
//    }
    //返回matrix[m-1][n-1],即两个字符串之间的距离
    return matrix[m-1][n-1];
}

//@func:求三个数的最小值
unsigned int min_zhengge(unsigned int x,unsigned int y,unsigned int z )
{
    unsigned int tmp=(x<y ? x:y);
    tmp=(tmp<z ? tmp:z);
    return tmp;
}
