package com.superpixel.jdot;

import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by montywest on 25/01/2017.
 */
public class TimingTest {


	@Test
	public void testCartManifest() {
		Map<String, String> pairs = new HashMap<>();
		pairs.put("identifier", "CartCard");
		pairs.put("image.image_url", "sku.image");
		pairs.put("line_item_id", "line_item.id");
		pairs.put("manfacturer.text", "product.attributes.Brand(Mothercare)");
		pairs.put("price.price.raw_price", "line_item.amount");
		pairs.put("price.price.text", "line_item.amount^cur<{|_{line_item.currency}}");
		pairs.put("quantity_picker.label.text", "|QTY x{line_item.quantity^d}");
		pairs.put("quantity_picker.max_value", "(20^n)");
		pairs.put("quantity_picker.min_value", "(1^n)");
		pairs.put("quantity_picker.options_label.text", "(CHANGE QUANTITY)");
		pairs.put("quantity_picker.selected_value", "line_item.quantity");
		pairs.put("quantity_picker.value_id", "(quantity)");
		pairs.put("remove_button.action.identifier", "(com.cartful.controller.CartViewController.RemoveFromCart)");
		pairs.put("remove_button.action.values.line_item_id", "line_item.id");
		pairs.put("remove_button.action.values.sku", "sku.id");
		pairs.put("remove_button.background_colour", "(#00000000)");
		pairs.put("remove_button.enabled", "(true^b)");
		pairs.put("remove_button.icon.colour", "(#8A8A8A)");
		pairs.put("remove_button.icon.icon", "(close-transparent)");
		pairs.put("sku_id", "sku.id");
		pairs.put("title.text", "product.name");


		{
			Stopwatch stopwatch = Stopwatch.createUnstarted();
			stopwatch.start();
			JvJDotTransformerBuilder builder = JvJDotTransformer.builder();
			stopwatch.stop();
			System.out.println("Base builder (SW:" + stopwatch + ")");

			stopwatch.reset().start();
			builder.withPathMapping(pairs);
			stopwatch.stop();
			System.out.println("Base pathMapping (SW:" + stopwatch + ")");

			stopwatch.reset().start();
			JvJDotTransformer t = builder.build();
			stopwatch.stop();
			System.out.println("Building transformer (SW:" + stopwatch + ")");
		}

		{
			Stopwatch stopwatch = Stopwatch.createUnstarted();
			stopwatch.start();
			JvJDotTransformerBuilder builder = JvJDotTransformer.builder();
			stopwatch.stop();
			System.out.println("Base builder (SW:" + stopwatch + ")");

			stopwatch.reset().start();
			builder.withPathMapping(pairs);
			stopwatch.stop();
			System.out.println("Base pathMapping (SW:" + stopwatch + ")");

			stopwatch.reset().start();
			JvJDotTransformer t = builder.build();
			stopwatch.stop();
			System.out.println("Building transformer (SW:" + stopwatch + ")");
		}
	}

}
