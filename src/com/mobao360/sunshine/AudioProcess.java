package com.mobao360.sunshine;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.lang.Short;

import com.mobao360.sunshine.R.string;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.media.AudioRecord;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

public class AudioProcess {
	public static final float pi= (float) 3.1415926;
	//Ӧ�ðѴ���ǰ���������߶���ʾ����
	private ArrayList<short[]> inBuf = new ArrayList<short[]>();//ԭʼ¼������
	private ArrayList<int[]> outBuf = new ArrayList<int[]>();//����������
	private boolean isRecording = false;

	//Context mContext = this.getContext();
	//�洢��ȡ��������
	//FileOutputStream fos;
	//������
	Context mContext;
	private int shift = 30;
	public int frequence = 0;
	
	private int length = 256;
	//y����С�ı���
	public int rateY = 21;
	//y�����
	public int baseLine = 0;
	//��ʼ����ͼ��һЩ����
	public void initDraw(int rateY, int baseLine,Context mContext, int frequence){
		this.mContext = mContext;
		this.rateY = rateY;
		this.baseLine = baseLine;
		this.frequence = frequence;
	}
	//��������
	public void start(AudioRecord audioRecord, int minBufferSize, SurfaceView sfvSurfaceView) {
		isRecording = true;
		new RecordThread(audioRecord, minBufferSize).start();
		//new ProcessThread().start();
		new DrawThread(sfvSurfaceView).start();
	}
	//ֹͣ����
	public void stop(SurfaceView sfvSurfaceView){
		isRecording = false;
		inBuf.clear();
		//sfvSurfaceView;
		//drawBuf.clear();
		//outBuf.clear();
	}
	
	//¼���߳�
	class RecordThread extends Thread{
		private AudioRecord audioRecord;
		private int minBufferSize;
		
		public RecordThread(AudioRecord audioRecord,int minBufferSize){
			this.audioRecord = audioRecord;
			this.minBufferSize = minBufferSize;
		}
		
		public void run(){
			try{
				short[] buffer = new short[minBufferSize];
				audioRecord.startRecording();
				//fos = mContext.openFileOutput("data.txt", Context.MODE_PRIVATE);
				while(isRecording){
					int res = audioRecord.read(buffer, 0, minBufferSize);
					//������д���ļ�,�Թ�����ʹ��
//					for(int i = 0; i < res; i++){
//						String str = Short.toString(buffer[i]);
//						fos.write(str.getBytes());
//						fos.write(' ');
//					}
//					fos.write('\n');
					//��¼�������ŵ�inBuf��,�Ա���ʱ��ͼʹ��
					synchronized (inBuf){
						inBuf.add(buffer);
					}
					//��֤����Ϊ2���ݴ���
					length=up2int(res);
					//length = 256;
					short[]tmpBuf = new short[length];
					System.arraycopy(buffer, 0, tmpBuf, 0, length);
					
					Complex[]complexs = new Complex[length];
					int[]outInt = new int[length];
					for(int i=0;i < length; i++){
						Short short1 = tmpBuf[i];
						complexs[i] = new Complex(short1.doubleValue());
					}
					fft(complexs,length);
					for (int i = 0; i < length; i++) {
						outInt[i] = complexs[i].getIntValue();
					}
					synchronized (outBuf) {
						outBuf.add(outInt);
					}
				}
//				try {
//					fos.close();
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//				}
				audioRecord.stop();
			}catch (Exception e) {
				// TODO: handle exception
				Log.i("Rec E",e.toString());
			}
			
		}
	}

	//��ͼ�߳�
	class DrawThread extends Thread{
		//����
		private SurfaceView sfvSurfaceView;
		//��ǰ��ͼ������Ļx�������
		//����
		private Paint mPaint;
		private Paint tPaint;
		private Paint dashPaint;
		public DrawThread(SurfaceView sfvSurfaceView) {
			this.sfvSurfaceView = sfvSurfaceView;
			//���û�������
			mPaint = new Paint();
			mPaint.setColor(Color.BLUE);
			mPaint.setStrokeWidth(2);
			mPaint.setAntiAlias(true);
			
			tPaint = new Paint();
			tPaint.setColor(Color.YELLOW);
			tPaint.setStrokeWidth(1);
			tPaint.setAntiAlias(true);
			
			dashPaint = new Paint();
			dashPaint.setStyle(Paint.Style.STROKE);
			dashPaint.setColor(Color.GRAY);
			Path path = new Path();
	        path.moveTo(0, 10);
	        path.lineTo(480,10); 
	        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
	        dashPaint.setPathEffect(effects);
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			while (isRecording) {
				ArrayList<int[]>buf = new ArrayList<int[]>();
				synchronized (outBuf) {
					if (outBuf.size() == 0) {
						continue;
					}
					buf = (ArrayList<int[]>)outBuf.clone();
					outBuf.clear();
				}
				//����ArrayList�е�short���鿪ʼ��ͼ
				for(int i = 0; i < buf.size(); i++){
					int[]tmpBuf = buf.get(i);
					SimpleDraw(tmpBuf, rateY, baseLine);
				}
				
			}
		}
		
		/** 
         * ����ָ������ 
         *  
         * @param start 
         *            X �Ὺʼ��λ��(ȫ��) 
         * @param buffer 
         *             ������ 
         * @param rate 
         *            Y ��������С�ı��� 
         * @param baseLine 
         *            Y ����� 
         */ 

		private void SimpleDraw(int[] buffer, int rate, int baseLine){
			Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
					new Rect(0, 0, buffer.length,sfvSurfaceView.getHeight()));
			canvas.drawColor(Color.BLACK);
			canvas.drawText("����ֵ", 0, 3, 2, 15, tPaint);
			canvas.drawText("ԭ��(0,0)", 0, 7, 5, baseLine + 15, tPaint);
			canvas.drawText("Ƶ��(HZ)", 0, 6, sfvSurfaceView.getWidth() - 50, baseLine + 30, tPaint);
			canvas.drawLine(shift, 20, shift, baseLine, tPaint);
			canvas.drawLine(shift, baseLine, sfvSurfaceView.getWidth(), baseLine, tPaint);
			canvas.save();
			canvas.rotate(30, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint);
			canvas.rotate(-60, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint);
			canvas.rotate(30, shift, 20);
			canvas.rotate(30, sfvSurfaceView.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceView.getWidth() - 1, baseLine, sfvSurfaceView.getWidth() - 11, baseLine, tPaint);
			canvas.rotate(-60, sfvSurfaceView.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceView.getWidth() - 1, baseLine, sfvSurfaceView.getWidth() - 11, baseLine, tPaint);
			canvas.restore();
			//tPaint.setStyle(Style.STROKE);
			for(int index = 64; index <= 512; index = index + 64){
				canvas.drawLine(shift + index, baseLine, shift + index, 40, dashPaint);
				String str = String.valueOf(frequence / 1024 * index);
				canvas.drawText( str, 0, str.length(), shift + index - 15, baseLine + 15, tPaint);
			}
			int y;
			for(int i = 0; i < buffer.length; i = i + 1){
				y = baseLine - buffer[i] / rateY ;
				canvas.drawLine(2*i + shift, baseLine, 2*i +shift, y, mPaint);
			}
			sfvSurfaceView.getHolder().unlockCanvasAndPost(canvas);
		}
	}
	
	/**
	 * ����ȡ��ӽ�iint��2���ݴ���.����iint=320ʱ,����256
	 * @param iint
	 * @return
	 */
	private int up2int(int iint) {
		int ret = 1;
		while (ret<=iint) {
			ret = ret << 1;
		}
		return ret>>1;
	}
	
	//���ٸ���Ҷ�任
	public void fft(Complex[] xin,int N)
	{
	    int f,m,N2,nm,i,k,j,L;//L:���㼶��
	    float p;
	    int e2,le,B,ip;
	    Complex w = new Complex();
	    Complex t = new Complex();
	    N2 = N / 2;//ÿһ���е��εĸ���,ͬʱҲ����mλ�����������λ��ʮ����Ȩֵ
	    f = N;//f��Ϊ�������̵ļ�����������
	    for(m = 1; (f = f / 2) != 1; m++);                             //�õ�����ͼ�Ĺ�����
	    nm = N - 2;
	    j = N2;
	    /******�������㡪���׵��㷨******/
	    for(i = 1; i <= nm; i++)
	    {
	        if(i < j)//��ֹ�ظ�����
	        {
	            t = xin[j];
	            xin[j] = xin[i];
	            xin[i] = t;
	        }
	        k = N2;
	        while(j >= k)
	        {
	            j = j - k;
	            k = k / 2;
	        }
	        j = j + k;
	    }
	    /******����ͼ���㲿��******/
	    for(L=1; L<=m; L++)                                    //�ӵ�1������m��
	    {
	    	e2 = (int) Math.pow(2, L);
	        //e2=(int)2.pow(L);
	        le=e2+1;
	        B=e2/2;
	        for(j=0;j<B;j++)                                    //j��0��2^(L-1)-1
	        {
	            p=2*pi/e2;
	            w.real = Math.cos(p * j);
	            //w.real=Math.cos((double)p*j);                                   //ϵ��W
	            w.image = Math.sin(p*j) * -1;
	            //w.imag = -sin(p*j);
	            for(i=j;i<N;i=i+e2)                                //���������ͬϵ��������
	            {
	                ip=i+B;                                           //��Ӧ���ε����ݼ��Ϊ2^(L-1)
	                t=xin[ip].cc(w);
	                xin[ip] = xin[i].cut(t);
	                xin[i] = xin[i].sum(t);
	            }
	        }
	    }
	}
}
