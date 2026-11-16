using System;
using Game1.Assets.src.g;

namespace Game1
{
	public static class SkhSlotEffect
	{
		public static int GetCanonicalSkhId(int optId)
		{
			switch (optId)
			{
			case 127:
			case 139:
				return 127; // Thên Xin Hăng
			case 128:
			case 140:
				return 128; // Kirin
			case 129:
			case 141:
				return 129; // Sôngôku
			case 130:
			case 142:
				return 130; // Picolo
			case 131:
			case 143:
				return 131; // Ốc Tiêu
			case 132:
			case 144:
				return 132; // Pikkoro Daimao
			case 133:
			case 136:
				return 133; // Kakarot
			case 134:
			case 137:
				return 134; // Ca Đíc
			case 135:
			case 138:
				return 135; // Nappa
			case 233:
			case 234:
				return 233; // Gohan
			case 237:
			case 238:
			case 239:
			case 240:
				return 237; // Nail (Mới)
			case 241:
			case 242:
			case 243:
			case 244:
				return 241; // Cađic M (Mới)
			case 245:
			case 246:
			case 247:
			case 248:
				return 245; // Kaio (Mới)
			default:
				return -1;
			}
		}

		public static int GetItemSkhId(Item item)
		{
			if (item == null || item.itemOption == null)
			{
				return -1;
			}
			for (int i = 0; i < item.itemOption.Length; i++)
			{
				ItemOption io = item.itemOption[i];
				if (io != null && io.optionTemplate != null)
				{
					int skh = GetCanonicalSkhId(io.optionTemplate.id);
					if (skh != -1)
					{
						return skh;
					}
				}
			}
			return -1;
		}

		public static bool IsNewSkhSet(int skhId)
		{
			return skhId == 237 || skhId == 241 || skhId == 245;
		}

		public static bool IsDoThanLinh(Item item)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			int id = item.template.id;
			return id >= 555 && id <= 567;
		}

		public static bool IsDoHuyDiet(Item item)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			int id = item.template.id;
			return id >= 650 && id <= 662;
		}

		public static bool IsDoThienSu(Item item)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			int id = item.template.id;
			return id >= 1048 && id <= 1062;
		}

		public static int GetFullSetTierFromBody(Item[] arrItemBody)
		{
			if (arrItemBody == null || arrItemBody.Length < 5)
			{
				return -1;
			}
			bool fullTS = true;
			bool fullHD = true;
			bool fullTL = true;
			for (int i = 0; i < 5; i++)
			{
				Item it = arrItemBody[i];
				if (it == null || it.template == null)
				{
					return -1;
				}
				if (!IsDoThienSu(it))
				{
					fullTS = false;
				}
				if (!IsDoHuyDiet(it))
				{
					fullHD = false;
				}
				if (!IsDoThanLinh(it))
				{
					fullTL = false;
				}
			}
			if (fullTS)
			{
				return 12;
			}
			if (fullHD)
			{
				return 11;
			}
			if (fullTL)
			{
				return 10;
			}
			return -1;
		}

		public static string GetFullSetTagName(int effSetId, int gender)
		{
			string planet;
			switch (gender)
			{
			case 0:
				planet = "Trái Đất";
				break;
			case 1:
				planet = "Namếc";
				break;
			case 2:
				planet = "Xayda";
				break;
			default:
				return null;
			}
			switch (effSetId)
			{
			case 10:
				return "Thần " + planet;
			case 11:
				return "Thần Hủy Diệt " + planet;
			case 12:
				return "Thiên Sứ " + planet;
			default:
				return null;
			}
		}

		public static void GetSetColors(int skhId, out int coreColor, out int mainColor, out int trailColor, out int pastelBg)
		{
			pastelBg = -1;
			switch (skhId)
			{
			case 129: // Sôngôku - Vàng Kim Hoàng Gia
				coreColor = 0xFFFFF0;
				mainColor = 0xFFD700;
				trailColor = 0xFFA000;
				break;
			case 128: // Kirin - Xanh Lam Kamehameha
				coreColor = 0xE8FFFF;
				mainColor = 0x00E5FF;
				trailColor = 0x0091EA;
				break;
			case 127: // Thên Xin Hăng - Cam Thái Dương Hạ San
				coreColor = 0xFFFDE7;
				mainColor = 0xFFAB40;
				trailColor = 0xFF6D00;
				break;
			case 130: // Picolo - Tím Ma Quang
				coreColor = 0xF8E8FF;
				mainColor = 0xE040FB;
				trailColor = 0x8E24AA;
				break;
			case 131: // Ốc Tiêu - Xanh Ngọc Lục Bảo
				coreColor = 0xE8FFF2;
				mainColor = 0x00E676;
				trailColor = 0x00A854;
				break;
			case 132: // Pikkoro Daimao - Đỏ Huyết Đệ Tử
				coreColor = 0xFFEBEB;
				mainColor = 0xFF5252;
				trailColor = 0xC62828;
				break;
			case 133: // Kakarot - Cam Đỏ Rực Cháy
				coreColor = 0xFFF3E0;
				mainColor = 0xFF6E40;
				trailColor = 0xDD2C00;
				break;
			case 134: // Ca Đíc - Xanh Dương Hoàng Tộc Xayda
				coreColor = 0xE8F0FF;
				mainColor = 0x448AFF;
				trailColor = 0x1565C0;
				break;
			case 135: // Nappa - Vàng Đồng Chiến Giáp
				coreColor = 0xFFF9E6;
				mainColor = 0xFFCA28;
				trailColor = 0xC68400;
				break;
			case 233: // Gohan - Xanh Chanh Kim Quang May Mắn
				coreColor = 0xF9FFE6;
				mainColor = 0xC6FF00;
				trailColor = 0x64DD17;
				break;
			case 245: // Kaio (Mới) - Nền Hồng Phấn Nhạt + Điện Đỏ Hồng Kaioken
				coreColor = 0xFFF0F5;
				mainColor = 0xFF4081;
				trailColor = 0xF50057;
				pastelBg = 0xFFCDD2;
				break;
			case 237: // Nail (Mới) - Nền Xanh Bạc Hà Nhạt + Điện Ngọc Bích
				coreColor = 0xE8FFFA;
				mainColor = 0x1DE9B6;
				trailColor = 0x00BFA5;
				pastelBg = 0xB2DFDB;
				break;
			case 241: // Cađic M (Mới) - Nền Tím Oải Hương Nhạt + Điện Tím Majin
				coreColor = 0xF3E8FF;
				mainColor = 0xB388FF;
				trailColor = 0x7C4DFF;
				pastelBg = 0xD1C4E9;
				break;
			default:
				coreColor = 0xFFFFFF;
				mainColor = 0x00E5FF;
				trailColor = 0x0099CC;
				break;
			}
		}

		public static bool PaintDragonBallSlotBg(mGraphics g, Item item, int x, int y, int w, int h)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			switch (item.template.id)
			{
			case 20: // Ngọc Rồng 7 sao: Nền đỏ nhạt
				g.setColor(0xFF9AA2, 0.42f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFFCDD2, 0.50f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return true;
			case 19: // Ngọc Rồng 6 sao: Nền xanh dương nhạt
				g.setColor(0x82CFFF, 0.42f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xBBDEFB, 0.50f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return true;
			case 16: // Ngọc Rồng 3 sao: Nền vàng
				g.setColor(0xFFE033, 0.42f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFFD54F, 0.60f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return true;
			case 15: // Ngọc Rồng 2 sao: Nền cam
				g.setColor(0xFF7700, 0.46f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFF9800, 0.65f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return true;
			case 14: // Ngọc Rồng 1 sao: Nền đỏ
				g.setColor(0xDC0019, 0.54f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFF1744, 0.75f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return true;
			default:
				return false;
			}
		}

		public static bool HasStarAura(Item item)
		{
			if (!ModFunc.isEffectInven || item == null || item.itemOption == null)
			{
				return false;
			}
			for (int i = 0; i < item.itemOption.Length; i++)
			{
				ItemOption io = item.itemOption[i];
				if (io != null && io.optionTemplate != null && io.param > 0 && (io.optionTemplate.id == 102 || io.optionTemplate.id == 72 || (io.optionTemplate.id >= 42 && io.optionTemplate.id <= 46)))
				{
					return true;
				}
			}
			return false;
		}

		public static void PaintSlotBg(mGraphics g, Item item, int x, int y, int w, int h)
		{
			if (item == null || w <= 4 || h <= 4)
			{
				return;
			}
			if (PaintDragonBallSlotBg(g, item, x, y, w, h))
			{
				return;
			}
			bool isTS = IsDoThienSu(item);
			bool isHD = IsDoHuyDiet(item);
			bool isTL = IsDoThanLinh(item);
			if (isTS)
			{
				// Đồ Thiên Sứ: Nền Lam Ngọc Bạch Kim - Ánh Vàng Thánh Quang
				g.setColor(0x00E5FF, 0.22f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFFF59D, 0.14f);
				g.fillRect(x + 3, y + 3, w - 6, h - 6);
				g.setColor(0x80FFFF, 0.72f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return;
			}
			if (isHD)
			{
				// Đồ Hủy Diệt: Nền Neon Tím Hồng Hakai
				g.setColor(0xE040FB, 0.22f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0x7C4DFF, 0.14f);
				g.fillRect(x + 3, y + 3, w - 6, h - 6);
				g.setColor(0xFF4081, 0.68f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return;
			}
			if (isTL)
			{
				// Đồ Thần Linh: Nền Hoàng Kim Thần Khí
				g.setColor(0xFFD54F, 0.20f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0x00E5FF, 0.10f);
				g.fillRect(x + 3, y + 3, w - 6, h - 6);
				g.setColor(0xFFCA28, 0.64f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
				return;
			}
			if (HasStarAura(item))
			{
				return;
			}
			int skhId = GetItemSkhId(item);
			if (skhId == -1)
			{
				return;
			}
			GetSetColors(skhId, out int _, out int mainColor, out int _, out int pastelBg);
			if (IsNewSkhSet(skhId) && pastelBg != -1)
			{
				// Set Kích Hoạt Mới: Nền màu nhạt thanh lịch
				g.setColor(pastelBg, 0.34f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(mainColor, 0.28f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
			}
		}

		public static void PaintSlotBorder(mGraphics g, Item item, int x, int y, int w, int h)
		{
			if (item == null || w <= 6 || h <= 6)
			{
				return;
			}
			bool isTS = IsDoThienSu(item);
			bool isHD = IsDoHuyDiet(item);
			bool isTL = IsDoThanLinh(item);
			int skhId = GetItemSkhId(item);
			if (skhId != -1)
			{
				PaintElectricBorderBySkhId(g, skhId, isTL, isHD || isTS, x, y, w, h);
				return;
			}
			if (isTS || isHD || isTL)
			{
				int core = isTS ? 0xFFFFFF : (isHD ? 0xFFE8FF : 0xFFFFE0);
				int main = isTS ? 0x80FFFF : (isHD ? 0xE040FB : 0xFFD54F);
				int trail = isTS ? 0xFFD740 : (isHD ? 0x7C4DFF : 0xFFA000);
				PaintTierShimmerBorder(g, core, main, trail, isTS ? 3 : 2, x, y, w, h);
			}
		}

		private static void PaintTierShimmerBorder(mGraphics g, int coreColor, int mainColor, int trailColor, int cometCount, int x, int y, int w, int h)
		{
			g.setColor(trailColor, 0.45f);
			g.drawRect(x, y, w - 1, h - 1);
			int perim = (w + h) * 2;
			if (perim <= 8)
			{
				return;
			}
			int baseTick = GameCanvas.gameTick * 2;
			int spacing = perim / cometCount;
			for (int c = 0; c < cometCount; c++)
			{
				int headPos = (baseTick + c * spacing) % perim;
				for (int seg = 3; seg >= 0; seg--)
				{
					int pos = (headPos - seg * 2 + perim * 4) % perim;
					GetPerimeterPoint(pos, x, y, w, h, out int px, out int py);
					if (seg == 0)
					{
						g.setColor(coreColor, 0.90f);
						g.fillRect(px, py, 2, 2);
					}
					else
					{
						g.setColor((seg == 1) ? mainColor : trailColor, (seg == 1) ? 0.75f : 0.45f);
						g.fillRect(px, py, 1, 1);
					}
				}
			}
		}

		private static void GetTierPalette(int effSetId, out int primary, out int secondary, out int core, out int badgeBorder)
		{
			switch (effSetId)
			{
			case 12: // Thiên Sứ
				primary = 0x00E5FF;
				secondary = 0xFFD740;
				core = 0xE8FFFF;
				badgeBorder = 0x80FFFF;
				break;
			case 11: // Hủy Diệt
				primary = 0xD500F9;
				secondary = 0xFF1744;
				core = 0xFF80AB;
				badgeBorder = 0xEA80FC;
				break;
			default: // 10: Thần Linh
				primary = 0xFFD700;
				secondary = 0x00E5FF;
				core = 0xFFF9C4;
				badgeBorder = 0xFFE57F;
				break;
			}
		}

		public static void PaintMapFullSetBehind(mGraphics g, int cx, int cy, int effSetId)
		{
			if (effSetId < 10 || effSetId > 12)
			{
				return;
			}
			GetTierPalette(effSetId, out int primary, out int secondary, out int core, out int _);
			int tick = GameCanvas.gameTick;
			float pulse = 0.85f + 0.15f * (float)System.Math.Sin(tick * 0.14);

			// Cột sáng / vầng nền thần khí phía sau lưng nhân vật
			if (!GameCanvas.lowGraphic)
			{
				if (effSetId == 12)
				{
					// Thiên Sứ: Trụ thánh quang thẳng đứng nhẹ nhàng
					g.setColor(primary, 0.10f * pulse);
					g.fillRect(cx - 16, cy - 58, 32, 60);
					g.setColor(core, 0.14f * pulse);
					g.fillRect(cx - 9, cy - 64, 18, 66);
				}
				else if (effSetId == 11)
				{
					// Hủy Diệt: Vầng năng lượng Hakai tím sẫm
					g.setColor(0x4A148C, 0.18f * pulse);
					g.fillRect(cx - 18, cy - 46, 36, 48);
					g.setColor(primary, 0.12f * pulse);
					g.fillRect(cx - 11, cy - 50, 22, 52);
				}
				else
				{
					// Thần Linh: Hào quang hoàng kim ấm
					g.setColor(primary, 0.12f * pulse);
					g.fillRect(cx - 15, cy - 44, 30, 46);
				}
			}

			// Pháp trận năng lượng elip dưới chân (vẽ bằng dải ngang mượt)
			int rxOuter = (effSetId == 12) ? 28 : ((effSetId == 11) ? 26 : 24);
			int ryOuter = 6;
			for (int dy = -ryOuter; dy <= ryOuter; dy++)
			{
				double norm = (double)dy / ryOuter;
				int halfW = (int)(rxOuter * System.Math.Sqrt(System.Math.Max(0.0, 1.0 - norm * norm)));
				if (halfW > 0)
				{
					g.setColor(primary, 0.16f * pulse);
					g.fillRect(cx - halfW, cy + dy - 1, halfW * 2, 1);
				}
			}
			int rxInner = rxOuter - 7;
			int ryInner = 4;
			for (int dy = -ryInner; dy <= ryInner; dy++)
			{
				double norm = (double)dy / ryInner;
				int halfW = (int)(rxInner * System.Math.Sqrt(System.Math.Max(0.0, 1.0 - norm * norm)));
				if (halfW > 0)
				{
					g.setColor(secondary, 0.22f * pulse);
					g.fillRect(cx - halfW, cy + dy - 1, halfW * 2, 1);
				}
			}
			g.setColor(core, 0.55f * pulse);
			g.fillRect(cx - rxOuter + 3, cy - 1, (rxOuter - 3) * 2, 2);

			// Các hạt tinh cầu quay nửa vòng sau lưng (sin < 0)
			if (!GameCanvas.lowGraphic)
			{
				int orbCount = (effSetId == 12) ? 6 : ((effSetId == 11) ? 5 : 4);
				double baseAngle = tick * ((effSetId == 12) ? 0.11 : 0.09);
				for (int i = 0; i < orbCount; i++)
				{
					double ang = baseAngle + i * (System.Math.PI * 2.0 / orbCount);
					double s = System.Math.Sin(ang);
					if (s < 0.0)
					{
						int ox = cx + (int)(System.Math.Cos(ang) * (rxOuter - 2));
						int oy = cy - 2 + (int)(s * 5.0);
						g.setColor((i % 2 == 0) ? primary : secondary, 0.75f);
						g.fillRect(ox - 1, oy - 1, 3, 3);
						g.setColor(core, 0.95f);
						g.fillRect(ox, oy, 1, 1);
					}
				}
			}
		}

		public static void PaintMapFullSetFront(mGraphics g, int cx, int cy, int effSetId)
		{
			if (effSetId < 10 || effSetId > 12 || GameCanvas.lowGraphic)
			{
				return;
			}
			GetTierPalette(effSetId, out int primary, out int secondary, out int core, out int _);
			int tick = GameCanvas.gameTick;
			int rxOuter = (effSetId == 12) ? 28 : ((effSetId == 11) ? 26 : 24);
			int orbCount = (effSetId == 12) ? 6 : ((effSetId == 11) ? 5 : 4);
			double baseAngle = tick * ((effSetId == 12) ? 0.11 : 0.09);

			// Các hạt tinh cầu quay nửa vòng phía trước nhân vật (sin >= 0)
			for (int i = 0; i < orbCount; i++)
			{
				double ang = baseAngle + i * (System.Math.PI * 2.0 / orbCount);
				double s = System.Math.Sin(ang);
				if (s >= 0.0)
				{
					int ox = cx + (int)(System.Math.Cos(ang) * (rxOuter - 2));
					int oy = cy - 1 + (int)(s * 5.0);
					g.setColor((i % 2 == 0) ? primary : secondary, 0.85f);
					g.fillRect(ox - 1, oy - 1, 3, 3);
					g.setColor(core);
					g.fillRect(ox, oy, 2, 2);
				}
			}

			// Hạt quang năng bay lên nhẹ quanh thân
			int moteCount = (effSetId == 12) ? 4 : 3;
			for (int m = 0; m < moteCount; m++)
			{
				int life = (tick * 2 + m * 17) % 44;
				float progress = life / 44f;
				int mx = cx + (int)(System.Math.Sin(tick * 0.08 + m * 2.1) * 15.0);
				int my = cy - 4 - (int)(progress * 42f);
				float alpha = (progress < 0.7f) ? 0.80f : ((1f - progress) * 2.6f);
				g.setColor((m % 2 == 0) ? core : primary, alpha);
				g.fillRect(mx, my, 2, 2);
			}
		}

		public static void PaintOverheadFullSetTag(mGraphics g, int cx, int yBottom, int effSetId, int gender)
		{
			string tag = GetFullSetTagName(effSetId, gender);
			if (string.IsNullOrEmpty(tag))
			{
				return;
			}
			GetTierPalette(effSetId, out int primary, out int secondary, out int core, out int badgeBorder);
			mFont font = (effSetId == 12) ? mFont.tahoma_7b_white : ((effSetId == 11) ? (mFont.tahoma_7b_pink ?? mFont.tahoma_7b_red) : mFont.tahoma_7b_yellow);
			if (font == null)
			{
				font = mFont.tahoma_7b_white;
			}
			int textW = font.getWidth(tag);
			int padX = 6;
			int boxW = textW + padX * 2;
			int boxH = 12;
			int bx = cx - boxW / 2;
			int by = yBottom - boxH;

			// Nền pill tối trong suốt + viền sáng theo bậc Set
			g.setColor(0x0B0E17, 0.78f);
			g.fillRect(bx, by, boxW, boxH);
			g.setColor(primary, 0.22f);
			g.fillRect(bx + 1, by + 1, boxW - 2, 4);
			g.setColor(badgeBorder, 0.88f);
			g.drawRect(bx, by, boxW - 1, boxH - 1);

			// Điểm nhấn 2 đầu hiệu hiệu ứng thần khí
			g.setColor(core, 0.95f);
			g.fillRect(bx - 1, by + boxH / 2 - 1, 2, 2);
			g.fillRect(bx + boxW - 1, by + boxH / 2 - 1, 2, 2);

			// Vệt sáng chạy ngang nhẹ trên viền trên của tag
			if (!GameCanvas.lowGraphic && boxW > 12)
			{
				int sweep = (GameCanvas.gameTick * 2) % (boxW - 6);
				g.setColor(secondary, 0.85f);
				g.fillRect(bx + 3 + sweep, by, 4, 1);
			}

			font.drawStringBorder(g, tag, cx, by + 1, mFont.CENTER, mFont.tahoma_7_greySmall);
		}

		public static void PaintInventoryFullSetSanctum(mGraphics g, int boxX, int boxY, int boxW, int boxH, int charX, int charY, int gender, int effSetId)
		{
			if (effSetId < 10 || effSetId > 12)
			{
				return;
			}
			GetTierPalette(effSetId, out int primary, out int secondary, out int core, out int badgeBorder);
			int tick = GameCanvas.gameTick;
			float pulse = 0.85f + 0.15f * (float)System.Math.Sin(tick * 0.12);

			// Khung điện thờ nền tối sang trọng phía sau nhân vật trong Hành Trang
			int bgDark = (effSetId == 12) ? 0x091E2C : ((effSetId == 11) ? 0x1A0926 : 0x241A06);
			g.setColor(bgDark, 0.78f);
			g.fillRect(boxX, boxY, boxW, boxH);
			g.setColor(primary, 0.14f * pulse);
			g.fillRect(boxX + 4, boxY + 4, boxW - 8, boxH - 8);
			g.setColor(secondary, 0.10f * pulse);
			g.fillRect(charX - 18, boxY + 6, 36, boxH - 10);

			// Viền khung + góc trang trí
			g.setColor(badgeBorder, 0.70f);
			g.drawRect(boxX, boxY, boxW - 1, boxH - 1);
			g.setColor(core, 0.90f);
			g.fillRect(boxX, boxY, 4, 1);
			g.fillRect(boxX, boxY, 1, 4);
			g.fillRect(boxX + boxW - 4, boxY, 4, 1);
			g.fillRect(boxX + boxW - 1, boxY, 1, 4);

			// Vòng pháp trận dưới chân nhân vật trong Hành Trang
			g.setColor(primary, 0.35f * pulse);
			g.fillRect(charX - 24, charY - 3, 48, 5);
			g.setColor(secondary, 0.45f * pulse);
			g.fillRect(charX - 16, charY - 2, 32, 3);
			g.setColor(core, 0.75f * pulse);
			g.fillRect(charX - 10, charY - 1, 20, 2);

			// Hạt sao lấp lánh trong khung nền
			for (int i = 0; i < 4; i++)
			{
				int life = (tick + i * 15) % 40;
				int sx = boxX + 10 + ((i * 23 + 7) % (boxW - 20));
				int sy = boxY + boxH - 8 - (life * (boxH - 18) / 40);
				g.setColor((i % 2 == 0) ? core : primary, 0.75f);
				g.fillRect(sx, sy, 2, 2);
			}
		}

		public static void PaintInventoryFullSetBanner(mGraphics g, int cx, int topY, int gender, int effSetId)
		{
			string tag = GetFullSetTagName(effSetId, gender);
			if (string.IsNullOrEmpty(tag))
			{
				return;
			}
			GetTierPalette(effSetId, out int primary, out int _, out int core, out int badgeBorder);
			mFont font = (effSetId == 12) ? mFont.tahoma_7b_white : ((effSetId == 11) ? (mFont.tahoma_7b_pink ?? mFont.tahoma_7b_red) : mFont.tahoma_7b_yellow);
			if (font == null)
			{
				font = mFont.tahoma_7b_white;
			}
			int w = System.Math.Min(98, font.getWidth(tag) + 8);
			int bx = cx - w / 2;
			g.setColor(0x0B0E17, 0.85f);
			g.fillRect(bx, topY, w, 11);
			g.setColor(primary, 0.22f);
			g.fillRect(bx + 1, topY + 1, w - 2, 3);
			g.setColor(badgeBorder, 0.85f);
			g.drawRect(bx, topY, w - 1, 10);
			g.setColor(core, 0.90f);
			g.fillRect(bx, topY + 5, 2, 1);
			g.fillRect(bx + w - 2, topY + 5, 2, 1);
			font.drawStringBorder(g, tag, cx, topY, mFont.CENTER, mFont.tahoma_7_greySmall);
		}

		public static void PaintElectricBorderBySkhId(mGraphics g, int skhId, bool isTL, bool isHD, int x, int y, int w, int h)
		{
			GetSetColors(skhId, out int coreColor, out int mainColor, out int trailColor, out int _);

			// Viền mảnh nhẹ theo màu Set
			g.setColor(trailColor, (isTL || isHD) ? 0.55f : 0.35f);
			g.drawRect(x, y, w - 1, h - 1);

			int perim = (w + h) * 2;
			if (perim <= 8)
			{
				return;
			}

			int cometCount = isHD ? 4 : ((isTL || IsNewSkhSet(skhId)) ? 3 : 2);
			int speed = isHD ? 3 : 2;
			int baseTick = GameCanvas.gameTick * speed;
			int spacing = perim / cometCount;

			for (int c = 0; c < cometCount; c++)
			{
				int headPos = (baseTick + c * spacing) % perim;
				// Vẽ đuôi tia điện chạy quanh viền ô
				for (int seg = 5; seg >= 0; seg--)
				{
					int pos = (headPos - seg * 2 + perim * 4) % perim;
					GetPerimeterPoint(pos, x, y, w, h, out int px, out int py);

					// Tạo độ giật tia điện nhỏ ở thân tia
					if (seg > 0 && seg < 4 && ((GameCanvas.gameTick + c + seg) % 3 == 0))
					{
						px += ((seg % 2 == 0) ? 1 : -1);
					}

					if (seg == 0)
					{
						g.setColor(mainColor, 0.85f);
						g.fillRect(px - 1, py - 1, 3, 3);
						g.setColor(coreColor);
						g.fillRect(px, py, 2, 2);
					}
					else if (seg <= 2)
					{
						g.setColor(mainColor, 0.90f);
						g.fillRect(px, py, 2, 2);
					}
					else
					{
						g.setColor(trailColor, 0.65f);
						g.fillRect(px, py, 1, 1);
					}
				}
			}
		}

		private static void GetPerimeterPoint(int pos, int x, int y, int w, int h, out int px, out int py)
		{
			if (pos < w)
			{
				px = x + pos;
				py = y;
			}
			else if (pos < w + h)
			{
				px = x + w - 1;
				py = y + (pos - w);
			}
			else if (pos < w * 2 + h)
			{
				px = x + w - 1 - (pos - (w + h));
				py = y + h - 1;
			}
			else
			{
				px = x;
				py = y + h - 1 - (pos - (w * 2 + h));
			}
		}
	}

	public class SkhCollectionUI
	{
		public const sbyte CMD_SKH_COLLECTION = 118;

		private const int NUM_SETS = 5;
		private const int NUM_SLOTS = 5;

		private static readonly string[] SLOT_LABELS = new string[5] { "Áo", "Quần", "Găng", "Giày", "Rada" };
		private static readonly short[,] PLACEHOLDER_ICONS = new short[3, 5]
		{
			{ 0, 6, 21, 27, 12 },   // Trái Đất starter item IDs -> looked up via ItemTemplates
			{ 1, 7, 22, 28, 12 },   // Namek
			{ 2, 8, 23, 29, 12 }    // Xayda
		};

		private static SkhCollectionUI instance;

		public bool isShow;
		public int activeTab;
		public int selectedSlot;
		public int completedSetCount;
		public bool isInHouse;

		private readonly string[] tabNames = new string[NUM_SETS];
		private readonly int[] tabSkhOptionIds = new int[NUM_SETS];
		private readonly int[] tabMountedCounts = new int[NUM_SETS];
		private readonly Item[,] mountedItems = new Item[NUM_SETS, NUM_SLOTS];

		public static SkhCollectionUI GI()
		{
			if (instance == null)
			{
				instance = new SkhCollectionUI();
			}
			return instance;
		}

		public SkhCollectionUI()
		{
			for (int t = 0; t < NUM_SETS; t++)
			{
				tabNames[t] = "Set " + (t + 1);
				tabSkhOptionIds[t] = 129;
			}
		}

		public void Close()
		{
			isShow = false;
			GameCanvas.clearKeyPressed();
			GameCanvas.clearKeyHold();
		}

		public void ReadMessage(Message msg)
		{
			try
			{
				sbyte sub = msg.reader().readByte();
				if (sub != 0)
				{
					return;
				}
				activeTab = msg.reader().readByte();
				if (activeTab < 0 || activeTab >= NUM_SETS)
				{
					activeTab = 0;
				}
				completedSetCount = msg.reader().readByte();
				isInHouse = msg.reader().readBoolean();

				for (int t = 0; t < NUM_SETS; t++)
				{
					tabNames[t] = msg.reader().readUTF();
					tabSkhOptionIds[t] = msg.reader().readShort();
					tabMountedCounts[t] = msg.reader().readByte();
					for (int s = 0; s < NUM_SLOTS; s++)
					{
						short tempId = msg.reader().readShort();
						if (tempId != -1)
						{
							short iconId = msg.reader().readShort();
							string itemName = msg.reader().readUTF();
							int optCount = msg.reader().readUnsignedByte();

							Item it = new Item();
							ItemTemplate tpl = ItemTemplates.get(tempId);
							if (tpl != null)
							{
								it.template = tpl;
							}
							else
							{
								it.template = new ItemTemplate(tempId, (sbyte)s, 0, itemName, string.Empty, 0, 0, iconId, -1, false);
							}
							it.quantity = 1;
							it.itemOption = new ItemOption[optCount];
							for (int k = 0; k < optCount; k++)
							{
								int optId = msg.reader().readShort();
								int param = msg.reader().readInt();
								it.itemOption[k] = new ItemOption(optId, param);
							}
							mountedItems[t, s] = it;
						}
						else
						{
							mountedItems[t, s] = null;
						}
					}
				}

				if (GameCanvas.panel != null && GameCanvas.panel.isShow)
				{
					GameCanvas.panel.hide();
				}
				isShow = true;
			}
			catch (Exception)
			{
			}
		}

		private void SendAction(int action, int arg1 = -1, int arg2 = -1)
		{
			Message m = null;
			try
			{
				m = new Message(CMD_SKH_COLLECTION);
				m.writer().writeByte(action);
				if (arg1 != -1)
				{
					m.writer().writeByte(arg1);
				}
				if (arg2 != -1)
				{
					m.writer().writeByte(arg2);
				}
				Session_ME.gI().sendMessage(m);
			}
			catch (Exception)
			{
			}
			finally
			{
				if (m != null)
				{
					m.cleanup();
				}
			}
		}

		private void ComputeLayout(
			out int uiX, out int uiY, out int uiW, out int uiH,
			out int leftX, out int bodyY, out int leftW,
			out int tabW, out int tabH,
			out int rackX, out int rackY, out int rackW, out int rackH,
			out int scrollX, out int scrollY, out int scrollW, out int scrollH,
			out int btnY, out int btnH)
		{
			uiW = System.Math.Min(416, GameCanvas.w - 8);
			uiH = System.Math.Min(232, GameCanvas.h - 6);
			uiX = (GameCanvas.w - uiW) / 2;
			uiY = (GameCanvas.h - uiH) / 2;

			bodyY = uiY + 24;
			scrollW = System.Math.Min(138, uiW * 35 / 100);
			leftW = uiW - scrollW - 8;
			leftX = uiX;

			tabW = 66;
			tabH = 27;

			rackX = leftX + tabW - 2;
			rackY = bodyY + 18;
			rackW = leftW - tabW + 2;
			rackH = 152;

			scrollX = leftX + leftW + 8;
			scrollY = bodyY + 2;
			scrollH = 166;

			btnY = rackY + rackH + 5;
			btnH = 22;
		}

		public void UpdateKey()
		{
			if (!isShow)
			{
				return;
			}

			ComputeLayout(
				out int uiX, out int uiY, out int uiW, out int uiH,
				out int leftX, out int bodyY, out int leftW,
				out int tabW, out int tabH,
				out int rackX, out int rackY, out int rackW, out int rackH,
				out int scrollX, out int scrollY, out int scrollW, out int scrollH,
				out int btnY, out int btnH);

			// Phím tắt bàn phím PC
			if (GameCanvas.keyPressed[2]) // Up
			{
				GameCanvas.keyPressed[2] = false;
				selectedSlot = (selectedSlot + NUM_SLOTS - 1) % NUM_SLOTS;
				return;
			}
			if (GameCanvas.keyPressed[8]) // Down
			{
				GameCanvas.keyPressed[8] = false;
				selectedSlot = (selectedSlot + 1) % NUM_SLOTS;
				return;
			}
			if (GameCanvas.keyPressed[4]) // Left
			{
				GameCanvas.keyPressed[4] = false;
				activeTab = (activeTab + NUM_SETS - 1) % NUM_SETS;
				return;
			}
			if (GameCanvas.keyPressed[6]) // Right
			{
				GameCanvas.keyPressed[6] = false;
				activeTab = (activeTab + 1) % NUM_SETS;
				return;
			}
			if (GameCanvas.keyPressed[5]) // Enter -> Gắn đồ
			{
				GameCanvas.keyPressed[5] = false;
				SendAction(1, activeTab);
				return;
			}
			if (GameCanvas.keyPressed[12] || GameCanvas.keyPressed[13])
			{
				GameCanvas.keyPressed[12] = false;
				GameCanvas.keyPressed[13] = false;
				Close();
				return;
			}

			if (!GameCanvas.isPointerJustRelease || !GameCanvas.isPointerClick)
			{
				return;
			}

			// Click chọn 1 trong 5 Tab Set bên trái
			for (int t = 0; t < NUM_SETS; t++)
			{
				int ty = rackY + 4 + t * (tabH + 2);
				if (GameCanvas.isPointerHoldIn(leftX, ty, tabW, tabH))
				{
					activeTab = t;
					GameCanvas.clearAllPointerEvent();
					return;
				}
			}

			// Click chọn 1 trong 5 Ô Trang Bị (Áo, Quần, Găng, Giày, Rada)
			for (int s = 0; s < NUM_SLOTS; s++)
			{
				int sy = rackY + 6 + s * 28;
				if (GameCanvas.isPointerHoldIn(rackX + 6, sy, rackW - 12, 26))
				{
					selectedSlot = s;
					GameCanvas.clearAllPointerEvent();
					return;
				}
			}

			// 4 Nút chức năng phía dưới: GẮN ĐỒ | THÁO ĐỒ | CẨM NANG | THOÁT
			int btnGap = 5;
			int btnW = System.Math.Min(64, (leftW - btnGap * 3) / 4);
			int startBtnX = leftX + (leftW - (btnW * 4 + btnGap * 3)) / 2;

			// Nút 0: GẮN ĐỒ
			if (GameCanvas.isPointerHoldIn(startBtnX, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				SendAction(1, activeTab);
				return;
			}

			// Nút 1: THÁO ĐỒ
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap), btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				if (mountedItems[activeTab, selectedSlot] != null)
				{
					SendAction(2, activeTab, selectedSlot);
				}
				else
				{
					GameScr.info1.addInfo("Ô [" + SLOT_LABELS[selectedSlot] + "] đang trống!", 0);
				}
				return;
			}

			// Nút 2: CẨM NANG
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap) * 2, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				SendAction(3);
				return;
			}

			// Nút 3: THOÁT
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap) * 3, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				Close();
				return;
			}

			// Click ra ngoài vùng bảng -> đóng bảng nhanh
			if (!GameCanvas.isPointerHoldIn(uiX - 4, uiY - 4, uiW + 8, uiH + 8))
			{
				GameCanvas.clearAllPointerEvent();
				Close();
				return;
			}

			GameCanvas.clearAllPointerEvent();
		}

		public void Paint(mGraphics g)
		{
			if (!isShow)
			{
				return;
			}

			g.translate(-g.getTranslateX(), -g.getTranslateY());
			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

			// Lớp nền tối mờ để làm nổi bật khung gỗ trên nền Nhà
			g.setColor(0x000000, 0.56f);
			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

			ComputeLayout(
				out int uiX, out int uiY, out int uiW, out int uiH,
				out int leftX, out int bodyY, out int leftW,
				out int tabW, out int tabH,
				out int rackX, out int rackY, out int rackW, out int rackH,
				out int scrollX, out int scrollY, out int scrollW, out int scrollH,
				out int btnY, out int btnH);

			// 1. Bảng gỗ tiêu đề trên cùng: SƯU TẬP SET KÍCH HOẠT
			int titleW = System.Math.Min(236, uiW - 20);
			int titleX = uiX + (uiW - titleW) / 2;
			PaintWoodBox(g, titleX, uiY, titleW, 21, 0x5B3516, 0x7E4B22, 0x261306);
			PaintGoldRivets(g, titleX, uiY, titleW, 21);
			mFont.tahoma_7b_yellow.drawString(g, "SƯU TẬP SET KÍCH HOẠT", titleX + titleW / 2, uiY + 5, mFont.CENTER);

			// 2. Thanh tiêu đề phụ khung trái: BẢNG GẮN ĐỒ & TAB (5 SET)
			PaintWoodBox(g, leftX + 6, bodyY, leftW - 10, 19, 0x472911, 0x663B19, 0x241205);
			mFont.tahoma_7b_white.drawString(g, "BẢNG GẮN ĐỒ & TAB (5 SET)", leftX + 6 + (leftW - 10) / 2, bodyY + 4, mFont.CENTER);

			// 3. Các Tab không được chọn (vẽ trước để nằm dưới mép bảng gỗ)
			for (int t = 0; t < NUM_SETS; t++)
			{
				if (t != activeTab)
				{
					PaintSetTab(g, t, leftX, rackY + 4 + t * (tabH + 2), tabW, tabH, isSelected: false);
				}
			}

			// 4. Bảng Gỗ Gắn Đồ chính
			PaintWoodBox(g, rackX, rackY, rackW, rackH, 0x7A4A21, 0x9B6334, 0x2D1708);

			// Vẽ Tab đang chọn đè liền mạch vào bảng gỗ
			int activeTabY = rackY + 4 + activeTab * (tabH + 2);
			PaintSetTab(g, activeTab, leftX, activeTabY, tabW + 3, tabH, isSelected: true);
			// Xóa đường viền ngăn cách giữa Tab đang chọn và Bảng gỗ để liền khối như ảnh mẫu
			g.setColor(0x7A4A21);
			g.fillRect(rackX, activeTabY + 2, 3, tabH - 4);

			// 5. Vẽ 5 Ô Trang Bị (Áo, Quần, Găng, Giày, Rada) dọc bên trái + thông tin bên phải
			int charGender = (Char.myCharz() != null && Char.myCharz().cgender >= 0 && Char.myCharz().cgender <= 2)
				? Char.myCharz().cgender
				: 0;

			for (int s = 0; s < NUM_SLOTS; s++)
			{
				int slotX = rackX + 8;
				int slotY = rackY + 6 + s * 28;
				int slotSize = 25;
				bool isSelSlot = (s == selectedSlot);
				Item mounted = mountedItems[activeTab, s];

				// Dòng nền bên cạnh ô đồ để dễ nhìn món đang chọn
				if (isSelSlot)
				{
					g.setColor(0xFFE082, 0.16f);
					g.fillRect(rackX + 5, slotY - 1, rackW - 10, slotSize + 2);
				}

				// Khung ô kem sáng viền gỗ như mockup
				g.setColor(isSelSlot ? 0xFFD54F : 0xEFE3C6);
				g.fillRect(slotX - 1, slotY - 1, slotSize + 2, slotSize + 2);
				g.setColor(mounted != null ? 0xB89F72 : 0xCBB58B);
				g.fillRect(slotX, slotY, slotSize, slotSize);
				g.setColor(0x4A2C11);
				g.drawRect(slotX, slotY, slotSize - 1, slotSize - 1);

				int textX = slotX + slotSize + 7;
				if (mounted != null && mounted.template != null)
				{
					SkhSlotEffect.PaintSlotBg(g, mounted, slotX, slotY, slotSize, slotSize);
					SmallImage.drawSmallImage(g, mounted.template.iconID, slotX + slotSize / 2, slotY + slotSize / 2, 0, 3);
					SkhSlotEffect.PaintSlotBorder(g, mounted, slotX, slotY, slotSize, slotSize);

					mFont nameFont = SkhSlotEffect.IsDoHuyDiet(mounted)
						? (mFont.tahoma_7b_pink ?? mFont.tahoma_7b_red ?? mFont.tahoma_7b_white)
						: (SkhSlotEffect.IsDoThanLinh(mounted) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_green2);
					nameFont.drawString(g, mounted.template.name, textX, slotY + 2, mFont.LEFT);
					mFont.tahoma_7_yellow.drawString(g, "Đã gắn (" + SLOT_LABELS[s] + ")", textX, slotY + 13, mFont.LEFT);
				}
				else
				{
					// Vẽ icon phôi mờ làm bóng gợi ý loại trang bị
					short sampleTplId = PLACEHOLDER_ICONS[charGender, s];
					ItemTemplate sampleTpl = ItemTemplates.get(sampleTplId);
					if (sampleTpl != null)
					{
						SmallImage.drawSmallImage(g, sampleTpl.iconID, slotX + slotSize / 2, slotY + slotSize / 2, 0, 3);
						g.setColor(0x5B3A1A, 0.58f);
						g.fillRect(slotX + 1, slotY + 1, slotSize - 2, slotSize - 2);
					}
					mFont.tahoma_7_white.drawString(g, "Ô " + SLOT_LABELS[s], textX, slotY + 2, mFont.LEFT);
					mFont.tahoma_7_grey.drawString(g, isInHouse ? "Chưa gắn đồ" : "Về Nhà để gắn", textX, slotY + 13, mFont.LEFT);
				}
			}

			// 6. Cuộn Giấy Da (Parchment Scroll) bên phải: THÀNH TỰU ĐỦ BỘ KÍCH HOẠT
			PaintParchmentScroll(g, scrollX, scrollY, scrollW, scrollH);

			int curCount = tabMountedCounts[activeTab];
			bool isTabFull = (curCount >= NUM_SLOTS);
			int scCenterX = scrollX + scrollW / 2;

			mFont.tahoma_7b_dark.drawString(g, "THÀNH TỰU:", scCenterX, scrollY + 12, mFont.CENTER);
			mFont.tahoma_7b_red.drawString(g, GetShortSetTitle(activeTab), scCenterX, scrollY + 23, mFont.CENTER);

			// Thanh tiến độ x/5
			int barW = scrollW - 20;
			int barH = 12;
			int barX = scrollX + 10;
			int barY = scrollY + 37;
			g.setColor(0x2C3E50);
			g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
			g.setColor(0x5D6D7E);
			g.fillRect(barX, barY, barW, barH);
			if (curCount > 0)
			{
				int fillW = barW * curCount / NUM_SLOTS;
				g.setColor(isTabFull ? 0x27AE60 : 0x2980B9);
				g.fillRect(barX, barY, fillW, barH);
				g.setColor(isTabFull ? 0x58D68D : 0x5DADE2, 0.55f);
				g.fillRect(barX, barY, fillW, barH / 2);
			}
			mFont.tahoma_7b_white.drawString(g, curCount + "/5", scCenterX, barY + 1, mFont.CENTER);

			// Các dòng chỉ số thưởng khi thu thập đủ trọn bộ 5/5 Set (25 món)
			bool isAllSetsFull = (completedSetCount >= NUM_SETS);
			int statX = scrollX + 8;
			int statY = scrollY + 54;
			mFont.tahoma_7b_dark.drawString(g, "ĐỦ 5/5 BỘ NHẬN:", statX, statY, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "+5% Sức đánh", statX + 1, statY + 14, mFont.LEFT);
			mFont.tahoma_7b_yellow.drawString(g, "+5% Sức đánh", statX, statY + 13, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "+15% TNSM", statX + 1, statY + 27, mFont.LEFT);
			mFont.tahoma_7b_green.drawString(g, "+15% TNSM", statX, statY + 26, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "(Sư phụ & Đệ tử)", statX, statY + 39, mFont.LEFT);
			mFont.tahoma_7b_blue.drawString(g, "+ Hào quang Vô Cực", statX, statY + 51, mFont.LEFT);

			// Vẽ Cúp Vàng bên phải (sáng vàng khi đủ cả 5/5 Bộ)
			PaintGoldenTrophy(g, scrollX + scrollW - 30, statY + 12, isAllSetsFull);

			// Tổng tiến độ 5 Set ở chân cuộn giấy
			g.setColor(0x8D6E43, 0.55f);
			g.fillRect(scrollX + 8, scrollY + scrollH - 35, scrollW - 16, 1);
			mFont.tahoma_7b_dark.drawString(g, "Tổng đủ: " + completedSetCount + "/5 Bộ", scCenterX, scrollY + scrollH - 32, mFont.CENTER);
			if (isAllSetsFull)
			{
				mFont.tahoma_7_blue1.drawString(g, "+5% SĐ | +15% TNSM", scCenterX, scrollY + scrollH - 21, mFont.CENTER);
			}
			else
			{
				mFont.tahoma_7_red.drawString(g, "Đủ 5/5 Bộ mở Buff", scCenterX, scrollY + scrollH - 21, mFont.CENTER);
			}

			// Dòng trạng thái kích hoạt bên dưới cuộn giấy
			int statusY = scrollY + scrollH + 3;
			if (isAllSetsFull)
			{
				mFont.tahoma_7_white.drawString(g, "(Đủ 5/5 Bộ - 25 món)", scCenterX, statusY, mFont.CENTER);
				mFont.tahoma_7b_green2.drawString(g, "ĐÃ KÍCH HOẠT BUFF", scCenterX, statusY + 11, mFont.CENTER);
			}
			else
			{
				mFont.tahoma_7_yellow.drawString(g, "(Bộ này: " + curCount + "/5 món)", scCenterX, statusY, mFont.CENTER);
				mFont.tahoma_7_grey.drawString(g, "CHƯA ĐỦ 5/5 BỘ", scCenterX, statusY + 11, mFont.CENTER);
			}

			// 7. Vẽ 4 Nút bấm phía dưới: GẮN ĐỒ | THÁO ĐỒ | CẨM NANG | THOÁT
			int btnGap = 5;
			int btnW = System.Math.Min(64, (leftW - btnGap * 3) / 4);
			int startBtnX = leftX + (leftW - (btnW * 4 + btnGap * 3)) / 2;
			bool canUnmount = (mountedItems[activeTab, selectedSlot] != null);

			PaintStylizedButton(g, startBtnX, btnY, btnW, btnH, "GẮN ĐỒ", 0x0E4D64, 0x1B7291, 0x062633, enabled: true);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap), btnY, btnW, btnH, "THÁO ĐỒ",
				canUnmount ? 0x8D5524 : 0x4A3B2C,
				canUnmount ? 0xB87333 : 0x5E4C3A,
				0x261608, enabled: canUnmount);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap) * 2, btnY, btnW, btnH, "CẨM NANG", 0x24476E, 0x356598, 0x102238, enabled: true);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap) * 3, btnY, btnW, btnH, "THOÁT", 0xC83E1D, 0xE65A37, 0x561607, enabled: true);
		}

		private string GetShortSetTitle(int tab)
		{
			string raw = tabNames[tab] ?? ("Set " + (tab + 1));
			if (raw.Length > 18)
			{
				return raw.Substring(0, 18);
			}
			return raw.ToUpper();
		}

		private string GetShortTabLabel(int tab)
		{
			string raw = tabNames[tab] ?? ("Set " + (tab + 1));
			if (raw.StartsWith("Set "))
			{
				raw = raw.Substring(4);
			}
			if (raw == "Thên Xin Hăng")
			{
				return "T.X.Hăng";
			}
			if (raw == "Pikkoro Daimao")
			{
				return "Pikkoro";
			}
			return raw;
		}

		private void PaintSetTab(mGraphics g, int tabIndex, int x, int y, int w, int h, bool isSelected)
		{
			int bg = isSelected ? 0x7A4A21 : 0x523015;
			int hi = isSelected ? 0x9B6334 : 0x6B3F1C;
			PaintWoodBox(g, x, y, w, h, bg, hi, 0x261306);

			int skhId = tabSkhOptionIds[tabIndex];
			SkhSlotEffect.GetSetColors(skhId, out int _, out int setMainColor, out int _, out int _);
			// Vạch màu đặc trưng của Set ở cạnh trái Tab
			g.setColor(setMainColor, isSelected ? 0.95f : 0.65f);
			g.fillRect(x + 2, y + 3, 3, h - 6);

			string label = GetShortTabLabel(tabIndex);
			mFont titleFont = isSelected ? mFont.tahoma_7b_white : mFont.tahoma_7_yellow;
			titleFont.drawString(g, label, x + 8, y + 3, mFont.LEFT);

			int count = tabMountedCounts[tabIndex];
			mFont countFont = (count >= NUM_SLOTS) ? mFont.tahoma_7b_green2 : mFont.tahoma_7_grey;
			countFont.drawString(g, count + "/5 món", x + 8, y + 14, mFont.LEFT);
		}

		private static void PaintWoodBox(mGraphics g, int x, int y, int w, int h, int fillHex, int highlightHex, int borderHex)
		{
			g.setColor(borderHex);
			g.fillRect(x, y, w, h);
			g.setColor(fillHex);
			g.fillRect(x + 1, y + 1, w - 2, h - 2);
			g.setColor(highlightHex, 0.7f);
			g.drawRect(x + 2, y + 2, w - 5, h - 5);
		}

		private static void PaintGoldRivets(mGraphics g, int x, int y, int w, int h)
		{
			g.setColor(0xFFD54F);
			g.fillRect(x + 4, y + 4, 2, 2);
			g.fillRect(x + w - 6, y + 4, 2, 2);
			g.fillRect(x + 4, y + h - 6, 2, 2);
			g.fillRect(x + w - 6, y + h - 6, 2, 2);
		}

		private static void PaintParchmentScroll(mGraphics g, int x, int y, int w, int h)
		{
			// Thân giấy da
			g.setColor(0x3E2611);
			g.fillRect(x, y + 6, w, h - 12);
			g.setColor(0xE8D2A2);
			g.fillRect(x + 1, y + 7, w - 2, h - 14);
			g.setColor(0xF5E7C6, 0.65f);
			g.fillRect(x + 4, y + 10, w - 8, h - 20);

			// Thanh cuộn trên & dưới
			int rollX = x - 3;
			int rollW = w + 6;
			int rollH = 9;
			// Cuộn trên
			g.setColor(0x3E2611);
			g.fillRect(rollX, y, rollW, rollH);
			g.setColor(0xD3B67E);
			g.fillRect(rollX + 1, y + 1, rollW - 2, rollH - 2);
			g.setColor(0xF2E0B6);
			g.fillRect(rollX + 2, y + 2, rollW - 4, 2);
			// Cuộn dưới
			int botY = y + h - rollH;
			g.setColor(0x3E2611);
			g.fillRect(rollX, botY, rollW, rollH);
			g.setColor(0xD3B67E);
			g.fillRect(rollX + 1, botY + 1, rollW - 2, rollH - 2);
			g.setColor(0xF2E0B6);
			g.fillRect(rollX + 2, botY + 2, rollW - 4, 2);
		}

		private static void PaintGoldenTrophy(mGraphics g, int x, int y, bool active)
		{
			int mainGold = active ? 0xFFC107 : 0x9E8C6C;
			int lightGold = active ? 0xFFF59D : 0xC2B59B;
			int darkGold = active ? 0xB26A00 : 0x5D5340;

			// Quai cúp hai bên
			g.setColor(darkGold);
			g.drawRect(x, y + 3, 19, 8);
			// Bầu cúp
			g.setColor(mainGold);
			g.fillRect(x + 3, y + 1, 14, 11);
			g.fillRect(x + 5, y + 12, 10, 3);
			// Ánh kim trên bầu cúp
			g.setColor(lightGold);
			g.fillRect(x + 5, y + 2, 4, 8);
			// Chân cúp & đế gỗ
			g.setColor(darkGold);
			g.fillRect(x + 8, y + 15, 4, 5);
			g.setColor(0x4E2F14);
			g.fillRect(x + 4, y + 20, 12, 4);
			g.setColor(mainGold);
			g.fillRect(x + 6, y + 21, 8, 2);

			// Hạt lấp lánh quanh cúp khi đã kích hoạt đủ 5/5
			if (active && (GameCanvas.gameTick / 6) % 2 == 0)
			{
				g.setColor(0xFFFFFF);
				g.fillRect(x + 1, y - 1, 2, 2);
				g.fillRect(x + 18, y + 13, 2, 2);
			}
		}

		private static void PaintStylizedButton(mGraphics g, int x, int y, int w, int h, string text, int fillHex, int topHiHex, int borderHex, bool enabled)
		{
			g.setColor(borderHex);
			g.fillRect(x, y, w, h);
			g.setColor(fillHex);
			g.fillRect(x + 1, y + 1, w - 2, h - 2);
			g.setColor(topHiHex);
			g.fillRect(x + 2, y + 2, w - 4, 3);

			mFont font = enabled ? mFont.tahoma_7b_yellow : mFont.tahoma_7_grey;
			if (enabled && (text == "THOÁT" || text == "CẨM NANG"))
			{
				font = mFont.tahoma_7b_white;
			}
			font.drawString(g, text, x + w / 2, y + (h - 11) / 2, mFont.CENTER);
		}
	}
}
