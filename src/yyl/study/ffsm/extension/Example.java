package yyl.study.ffsm.extension;

public class Example {

	
	//类层次的基类
	static abstract class Figure{
		abstract double area();
	}
	
	//圆形类
	static class Circle extends Figure{
		final double radius;
		
		Circle(double radius){
			this.radius=radius;
		}
		
		double area(){
			return Math.PI*(radius*radius);
		}
	}
	
	//矩形类
	static class Rectangle extends Figure{
		final double length;
		final double width;
		
		//表示矩形
		Rectangle(double length,double width){
			this.length=length;
			this.width=width;
		}
		
		double area(){
			return this.length*this.width;
		}
	}
	
	//正方形
	static class Square extends Rectangle{
		Square(double length,double width){
			super(length,width);
		}
		
	}

	
}
