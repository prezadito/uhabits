#!/usr/bin/env python3
"""Sorts the bundled icons into the categories of the icon picker.

Each icon gets exactly one category. An icon is scored against every category by the words in its
name (strong) and, only if no name word matches anything, by its search keywords (weak). Ties go to
the category listed first in CATEGORIES, with QUIT_LIMIT before all others. Icons that match nothing
are "SYMBOLS_MORE". OVERRIDES wins over the rules.

The category ids are the names of the enum IconCategory in uhabits-core.

Usage (offline, from the icon data already in the repo):
  tools/icon_categories.py            rewrites the two *_icon_categories.txt files, prints counts
  tools/icon_categories.py --list ID  prints the icons of one category
"""
import sys
from collections import Counter
from pathlib import Path

RAW = Path(__file__).resolve().parent.parent / "uhabits-android" / "src" / "main" / "res" / "raw"

FALLBACK = "SYMBOLS_MORE"


def w(text: str) -> set[str]:
    return set(text.split())


# Words that, found in an icon name, put it in a category. Order is the display order.
CATEGORIES: dict[str, set[str]] = {
    "FITNESS_SPORTS": w(
        """fitness dumbbell gym run running runner walk walking hiking hike bike biking bicycle
        cycling pool swim swimming sport soccer football basketball baseball tennis golf volleyball
        cricket hockey rugby kayaking skateboarding skiing snowboarding sledding surfing rowing
        martial boxing kabaddi handball gymnastics weight weightlifting exercise workout stadium
        trophy medal bowling pingpong athletic muscle strength sprint jog stretch
        trekking climbing"""
    ),
    "HEALTH_CARE": w(
        """health healthcare medical medication medicine pill vaccine vaccines syringe hospital
        stethoscope bandage healing tooth teeth toothbrush dental wash washing sanitizer
        bath bathtub soap hygiene mask thermometer bloodtype blood clinic ambulance wheelchair
        accessible glasses eyeglasses hearing vitamin emergency virus coronavirus lungs
        bone heartbeat pulse heart-pulse kit-medical user-doctor doctor nurse prescription capsule
        pregnant diagnosis skincare lotion sunscreen"""
    ),
    "MIND_SLEEP": w(
        """sleep sleeping bed bedtime bedroom night nightlight moon meditation meditate
        self_improvement spa psychology brain mood mindfulness yoga pray praying prayer church mosque
        synagogue temple relax calm peace snooze emotion emotional sentiment face smile happy sad
        therapy mental dream lotus om peaceful hotel zzz"""
    ),
    "FOOD_DRINK": w(
        """restaurant food fastfood lunch dinner breakfast brunch cafe coffee tea drink drinks
        water wine beer liquor cocktail bakery cake cookie pizza burger hamburger ramen rice egg soup
        icecream cream apple lemon carrot fish bowl utensils spoon nutrition meal
        takeout dining cooking bread cheese candy lollipop pepper cup mug bottle blender grocery
        salad lunch_dining dinner_dining kitchen bacon drumstick hotdog mug-hot jar whiskey
        glass-water martini bowl-food bowl-rice cookie-bite stroopwafel plate"""
    ),
    "LEARNING_WORK": w(
        """school book books menu_book auto_stories library read reading edit_note note notes pen
        pencil write writing article assignment class classroom education study science biotech
        calculate calculator math functions laptop computer code terminal work business briefcase
        office desk meeting email mail inbox task tasks checklist language translate lightbulb lecture
        graduation quiz presentation slideshow project folder file document print badge
        graduation-cap chalkboard diploma notebook journal diary microscope flask atom university
        lab research"""
    ),
    "HOME_CHORES": w(
        """home house cleaning clean broom vacuum mop trash recycling recycle laundry iron
        dry_cleaning chair couch weekend window door garage yard lawn plumbing handyman build
        hammer wrench tool tools screwdriver toolbox shopping cart store storefront mall bag basket
        dishwasher microwave countertops bathroom lamp fireplace roofing construction carpenter
        electrical hvac sink toilet checkroom inventory shirt tshirt soap-dispenser pump-soap
        fan blinds curtains doorbell
        """
    ),
    "MONEY": w(
        """money attach_money savings payments payment credit_card account_balance wallet paid price
        currency dollar euro coins coin bank atm receipt request_quote trending finance
        show_chart sell diamond cash invoice sack piggy-bank vault landmark cash-register
        money-bill money-check hand-holding-dollar yen pound rupee bitcoin wallet
        """
    ),
    "FAMILY_FRIENDS": w(
        """family child children child_care baby baby-carriage boy girl man woman elderly people
        group groups diversity handshake favorite heart volunteer_activism pets pet dog cat call
        phone chat forum message sms social supervisor_account couple birthday celebration party
        contacts person_add friend friends hug people-group kiss love gift
        child-reaching people-roof wc
        """
    ),
    "HOBBIES_CREATIVITY": w(
        """palette brush paint color colors draw drawing art music music_note audiotrack piano guitar
        headphones headset album library_music mic microphone theater theaters movie film games game
        gamepad esports videogame casino dice puzzle extension chess toys camera photo_camera
        photography camera_alt collections festival interests sewing design_services architecture
        podcasts tv craft paintbrush drum masks-theater wand-magic magic palette-brush
        brush-paint ticket video_library crop_original saxophone violin
        """
    ),
    "NATURE_TRAVEL": w(
        """nature park forest eco grass landscape terrain camping cabin tree trees florist flower
        agriculture flight airplane plane luggage travel trip explore map public earth beach
        sunny wb_sunny rain umbrella thunderstorm snow snowing weather air wind mountain
        volcano campfire fire fire_department sailing boat ship train tram subway car bus taxi
        commute motorcycle rv_hookup passport navigation place location compass bug bird fish paw
        leaf seedling sun globe road route
        """
    ),
    "QUIT_LIMIT": w(
        """ban smoke smoking smokefree vape vaping cigarette cannabis
        joint bong smoke_free vape_free ban-smoking
        """
    ),
    "TIME_ROUTINE": w(
        """alarm schedule time timer timelapse hourglass watch clock history today
        event calendar date_range repeat loop replay snooze pending_actions more_time av_timer
        query_builder next_plan stopwatch bell notifications reminder routine sunrise sunset
        twilight wb_twilight
        """
    ),
}

# Icons whose name alone would send them to the wrong category, or that people use as habit
# icons and should be easy to find.
OVERRIDES = {
    "library_music": "HOBBIES_CREATIVITY", "my_library_music": "HOBBIES_CREATIVITY",
    "photo_library": "HOBBIES_CREATIVITY", "video_library": "HOBBIES_CREATIVITY",
    "keyboard_voice": "SYMBOLS_MORE", "co2": "SYMBOLS_MORE", "fa:shoe-prints": "FITNESS_SPORTS",
    "pan_tool": "SYMBOLS_MORE", "pan_tool_alt": "SYMBOLS_MORE", "mode_fan_off": "SYMBOLS_MORE",
    "mode_standby": "SYMBOLS_MORE", "swap_horizontal_circle": "SYMBOLS_MORE",
    "energy_savings_leaf": "NATURE_TRAVEL", "production_quantity_limits": "SYMBOLS_MORE",
    "fa:diamond-turn-right": "SYMBOLS_MORE", "airline_seat_flat": "NATURE_TRAVEL",
    "airline_seat_flat_angled": "NATURE_TRAVEL", "airline_seat_individual_suite": "NATURE_TRAVEL",
    "flight_class": "NATURE_TRAVEL", "fa:code-branch": "SYMBOLS_MORE",
    "fa:layer-group": "SYMBOLS_MORE", "fa:object-group": "SYMBOLS_MORE",
    "fa:paper-plane": "SYMBOLS_MORE", "account_tree": "SYMBOLS_MORE",
    "fa:radio": "HOBBIES_CREATIVITY", "radio": "HOBBIES_CREATIVITY",
    "cloud": "NATURE_TRAVEL", "fa:cloud": "NATURE_TRAVEL", "bug_report": "SYMBOLS_MORE",
    "fa:people-robbery": "SYMBOLS_MORE", "fa:people-pulling": "SYMBOLS_MORE",
    "change_history": "SYMBOLS_MORE", "tips_and_updates": "LEARNING_WORK",
    "line_weight": "SYMBOLS_MORE", "waves": "NATURE_TRAVEL", "moped": "NATURE_TRAVEL", "female": "SYMBOLS_MORE",
    "male": "SYMBOLS_MORE", "10k": "SYMBOLS_MORE", "fitbit": "SYMBOLS_MORE",
    "running_with_errors": "SYMBOLS_MORE", "fa:masks-theater": "HOBBIES_CREATIVITY",
    "local_car_wash": "HOME_CHORES", "fa:person-walking-luggage": "NATURE_TRAVEL",
    "fa:person-walking-arrow-loop-left": "SYMBOLS_MORE",
    "fa:person-walking-dashed-line-arrow-right": "SYMBOLS_MORE",
    "fa:person-walking-with-cane": "HEALTH_CARE", "fa:cloud-showers-heavy": "NATURE_TRAVEL",
    "fa:cloud-showers-water": "NATURE_TRAVEL", "fa:faucet": "HOME_CHORES",
    "fa:faucet-drip": "HOME_CHORES", "emoji_objects": "LEARNING_WORK",
    "emoji_symbols": "SYMBOLS_MORE", "hotel_class": "SYMBOLS_MORE", "sick": "HEALTH_CARE",
    "face": "SYMBOLS_MORE", "face_2": "SYMBOLS_MORE", "face_3": "SYMBOLS_MORE",
    "face_4": "SYMBOLS_MORE", "face_5": "SYMBOLS_MORE", "face_6": "SYMBOLS_MORE",
    "face_retouching_off": "SYMBOLS_MORE", "fa:code-fork": "SYMBOLS_MORE",
    "monitor_weight": "HEALTH_CARE", "fa:weight-scale": "HEALTH_CARE",
    "military_tech": "FITNESS_SPORTS", "sports_score": "SYMBOLS_MORE",
    "sports_motorsports": "HOBBIES_CREATIVITY", "ice_skating": "FITNESS_SPORTS",
    "hot_tub": "HEALTH_CARE", "nightlife": "HOBBIES_CREATIVITY",
    # quit and limit
    "no_adult_content": "QUIT_LIMIT", "18_up_rating": "QUIT_LIMIT", "no_drinks": "QUIT_LIMIT",
    "no_food": "QUIT_LIMIT", "no_meals": "QUIT_LIMIT", "no_cell": "QUIT_LIMIT",
    "no_photography": "QUIT_LIMIT", "do_not_disturb": "QUIT_LIMIT", "do_not_touch": "QUIT_LIMIT",
    "smoking_rooms": "QUIT_LIMIT", "smoke_free": "QUIT_LIMIT", "vape_free": "QUIT_LIMIT",
    "vaping_rooms": "QUIT_LIMIT", "block": "QUIT_LIMIT", "front_hand": "QUIT_LIMIT",
    "phonelink_erase": "QUIT_LIMIT", "phone_disabled": "QUIT_LIMIT", "mobile_off": "QUIT_LIMIT",
    "fa:ban": "QUIT_LIMIT", "fa:ban-smoking": "QUIT_LIMIT", "fa:cannabis": "QUIT_LIMIT",
    "fa:joint": "QUIT_LIMIT", "fa:bong": "QUIT_LIMIT", "fa:eye-slash": "QUIT_LIMIT",
    "fa:smoking": "QUIT_LIMIT", "fa:phone-slash": "QUIT_LIMIT",
    "fa:wine-bottle": "QUIT_LIMIT",
    "local_bar": "QUIT_LIMIT", "sports_bar": "QUIT_LIMIT", "liquor": "QUIT_LIMIT",
    "wine_bar": "QUIT_LIMIT", "fa:beer-mug-empty": "QUIT_LIMIT", "fa:wine-glass": "QUIT_LIMIT",
    "fa:martini-glass": "QUIT_LIMIT", "fa:champagne-glasses": "QUIT_LIMIT",
    # health
    "monitor_heart": "HEALTH_CARE", "fa:heart-pulse": "HEALTH_CARE", "fa:tooth": "HEALTH_CARE",
    "face_retouching_natural": "HEALTH_CARE", "water_drop": "FOOD_DRINK",
    "fa:droplet": "FOOD_DRINK", "fa:glass-water": "FOOD_DRINK",
    "fa:glass-water-droplet": "FOOD_DRINK", "hand_hygiene": "HEALTH_CARE",
    "clean_hands": "HEALTH_CARE", "wash": "HEALTH_CARE", "sanitizer": "HEALTH_CARE",
    "fa:hands-bubbles": "HEALTH_CARE", "fa:soap": "HEALTH_CARE", "fa:pump-soap": "HEALTH_CARE",
    "fa:bath": "HEALTH_CARE", "fa:shower": "HEALTH_CARE", "shower": "HEALTH_CARE",
    "bathtub": "HEALTH_CARE", "fa:toothbrush": "HEALTH_CARE", "fa:eye": "HEALTH_CARE",
    "visibility": "SYMBOLS_MORE", "visibility_off": "SYMBOLS_MORE",
    # mind and sleep
    "self_improvement": "MIND_SLEEP", "bedtime": "MIND_SLEEP", "bedtime_off": "MIND_SLEEP",
    "nights_stay": "MIND_SLEEP", "dark_mode": "MIND_SLEEP", "mode_night": "MIND_SLEEP",
    "fa:bed": "MIND_SLEEP", "fa:moon": "MIND_SLEEP", "fa:brain": "MIND_SLEEP",
    "fa:person-praying": "MIND_SLEEP", "fa:hands-praying": "MIND_SLEEP",
    "fa:spa": "MIND_SLEEP", "fa:om": "MIND_SLEEP", "fa:peace": "MIND_SLEEP",
    "fa:yin-yang": "MIND_SLEEP", "fa:lotus": "MIND_SLEEP", "hotel": "MIND_SLEEP",
    # fitness
    "emoji_events": "FITNESS_SPORTS", "fa:trophy": "FITNESS_SPORTS", "fa:medal": "FITNESS_SPORTS",
    "directions_run": "FITNESS_SPORTS", "directions_walk": "FITNESS_SPORTS",
    "directions_bike": "FITNESS_SPORTS", "pedal_bike": "FITNESS_SPORTS", "pool": "FITNESS_SPORTS",
    "hiking": "FITNESS_SPORTS", "fa:person-hiking": "FITNESS_SPORTS",
    "fa:person-running": "FITNESS_SPORTS", "fa:person-walking": "FITNESS_SPORTS",
    "fa:person-biking": "FITNESS_SPORTS", "fa:person-swimming": "FITNESS_SPORTS",
    "fa:dumbbell": "FITNESS_SPORTS", "fa:weight-hanging": "FITNESS_SPORTS",
    "fa:heart-circle-bolt": "FITNESS_SPORTS", "fa:stopwatch": "TIME_ROUTINE",
    "sports_esports": "HOBBIES_CREATIVITY", "fa:table-tennis-paddle-ball": "FITNESS_SPORTS",
    # food and drink
    "local_cafe": "FOOD_DRINK", "free_breakfast": "FOOD_DRINK", "coffee": "FOOD_DRINK",
    "fa:mug-hot": "FOOD_DRINK", "fa:mug-saucer": "FOOD_DRINK", "water": "NATURE_TRAVEL",
    "set_meal": "FOOD_DRINK", "kitchen": "HOME_CHORES", "fa:kitchen-set": "HOME_CHORES",
    "grocery": "HOME_CHORES", "local_grocery_store": "HOME_CHORES", "shopping_basket": "HOME_CHORES",
    # learning and work
    "psychology": "MIND_SLEEP", "psychology_alt": "MIND_SLEEP", "edit": "SYMBOLS_MORE",
    "edit_note": "LEARNING_WORK", "menu_book": "LEARNING_WORK", "school": "LEARNING_WORK",
    "lightbulb": "LEARNING_WORK", "fa:lightbulb": "LEARNING_WORK", "fa:book": "LEARNING_WORK",
    "fa:book-open": "LEARNING_WORK", "language": "LEARNING_WORK", "fa:language": "LEARNING_WORK",
    "translate": "LEARNING_WORK", "public": "NATURE_TRAVEL", "fa:earth-americas": "NATURE_TRAVEL",
    "fa:globe": "NATURE_TRAVEL", "work": "LEARNING_WORK", "fa:briefcase": "LEARNING_WORK",
    # home and chores
    "cleaning_services": "HOME_CHORES", "local_laundry_service": "HOME_CHORES",
    "fa:broom": "HOME_CHORES", "fa:spray-can-sparkles": "HOME_CHORES",
    "fa:shirt": "HOME_CHORES", "dry_cleaning": "HOME_CHORES", "delete": "HOME_CHORES",
    "delete_outline": "HOME_CHORES", "fa:trash": "HOME_CHORES", "fa:recycle": "HOME_CHORES",
    "key": "HOME_CHORES", "lock": "SYMBOLS_MORE", "lock_open": "SYMBOLS_MORE",
    # money
    "savings": "MONEY", "paid": "MONEY", "fa:piggy-bank": "MONEY", "fa:sack-dollar": "MONEY",
    "account_balance": "MONEY", "account_balance_wallet": "MONEY", "fa:wallet": "MONEY",
    # family and friends
    "favorite": "FAMILY_FRIENDS", "favorite_border": "FAMILY_FRIENDS", "fa:heart": "FAMILY_FRIENDS",
    "fa:people-group": "FAMILY_FRIENDS", "fa:users": "FAMILY_FRIENDS",
    "call": "FAMILY_FRIENDS", "fa:phone": "FAMILY_FRIENDS", "fa:envelope": "FAMILY_FRIENDS",
    "star": "SYMBOLS_MORE", "star_border": "SYMBOLS_MORE", "fa:star": "SYMBOLS_MORE",
    # hobbies
    "music_note": "HOBBIES_CREATIVITY", "fa:music": "HOBBIES_CREATIVITY",
    "fa:guitar": "HOBBIES_CREATIVITY", "fa:palette": "HOBBIES_CREATIVITY",
    "fa:paintbrush": "HOBBIES_CREATIVITY", "fa:camera": "HOBBIES_CREATIVITY",
    # nature and travel
    "wb_sunny": "NATURE_TRAVEL", "light_mode": "NATURE_TRAVEL", "fa:sun": "NATURE_TRAVEL",
    "fa:tree": "NATURE_TRAVEL", "fa:leaf": "NATURE_TRAVEL", "fa:seedling": "NATURE_TRAVEL",
    "fa:plane": "NATURE_TRAVEL", "fa:car": "NATURE_TRAVEL", "fa:mountain": "NATURE_TRAVEL",
    "fa:paw": "FAMILY_FRIENDS", "fa:dog": "FAMILY_FRIENDS", "fa:cat": "FAMILY_FRIENDS",
    # time and routine
    "alarm": "TIME_ROUTINE", "alarm_on": "TIME_ROUTINE", "schedule": "TIME_ROUTINE",
    "access_time": "TIME_ROUTINE", "timer": "TIME_ROUTINE", "today": "TIME_ROUTINE",
    "event_repeat": "TIME_ROUTINE", "fa:clock": "TIME_ROUTINE", "fa:calendar": "TIME_ROUTINE",
    "fa:hourglass": "TIME_ROUTINE", "fa:bell": "TIME_ROUTINE", "fa:repeat": "TIME_ROUTINE",
}

# Name words that are too generic to decide on their own: they only count when nothing else
# in the name matches.
WEAK = w("hand hands heart bar water cup fire air car map place location event update history")

PRIORITY = ["QUIT_LIMIT"] + [c for c in CATEGORIES if c != "QUIT_LIMIT"]


def singular(word: str) -> str:
    return word[:-1] if len(word) > 3 and word.endswith("s") and not word.endswith("ss") else word


def tokens(text: str) -> list[str]:
    cleaned = text.lower().replace("_", " ").replace("-", " ").replace(":", " ")
    return [singular(t) for t in cleaned.split() if t]


_WORD_SETS = {
    cat: {singular(word) for word in words if "-" not in word and "_" not in word}
    for cat, words in CATEGORIES.items()
}

# Search keywords are noisy ("check" is tagged "payment"), so only these count, and only for icons
# whose name matched nothing.
STRONG_KEYWORDS = {
    "FITNESS_SPORTS": w("gym workout exercise jog jogging cardio"),
    "HEALTH_CARE": w("medicine medical doctor dental floss flossing hygiene"),
    "MIND_SLEEP": w("meditation meditate mindfulness sleep"),
    "FOOD_DRINK": w("food snack dessert coffee"),
    "MONEY": w("money cash currency savings"),
    "QUIT_LIMIT": w("porn pornography nofap nsfw cigarette cigarettes tobacco vape vaping weed"),
    "LEARNING_WORK": w("study homework"),
}


def categorize(name: str, keywords: str) -> str:
    """The category id of the icon [name] (Font Awesome names start with "fa:")."""
    if name in OVERRIDES:
        return OVERRIDES[name]
    name_tokens = tokens(name.removeprefix("fa:"))
    best, best_score = FALLBACK, 0
    for cat in PRIORITY:
        score = sum(1 for t in name_tokens if t in _WORD_SETS[cat] and t not in WEAK)
        score += sum(0.5 for t in name_tokens if t in _WORD_SETS[cat] and t in WEAK)
        if score > best_score:
            best, best_score = cat, score
    if best_score > 0:
        return best
    keyword_tokens = set(tokens(keywords))
    for cat in PRIORITY:
        if keyword_tokens & STRONG_KEYWORDS.get(cat, set()):
            return cat
    return best


def write_files(material: list[tuple[str, str]], fa: list[tuple[str, str]]) -> Counter:
    """Writes the two category files from (name, keywords) lists; returns the counts."""
    counts: Counter = Counter()
    for filename, icons, prefix in (
        ("material_icon_categories.txt", material, ""),
        ("fa_icon_categories.txt", fa, "fa:"),
    ):
        lines = []
        for name, keywords in icons:
            cat = categorize(prefix + name, keywords)
            counts[cat] += 1
            lines.append(f"{name}|{cat}")
        (RAW / filename).write_text("\n".join(lines) + "\n")
    return counts


def read_existing() -> tuple[list[tuple[str, str]], list[tuple[str, str]]]:
    def split(line: str) -> tuple[str, str]:
        head, _, words = line.rstrip("\n").partition("|")
        return head.split()[0], words

    material = [split(l) for l in (RAW / "material_icon_keywords.txt").read_text().splitlines() if l]
    fa = [split(l) for l in (RAW / "fa_icon_codepoints.txt").read_text().splitlines() if l]
    return material, fa


def main() -> None:
    material, fa = read_existing()
    if len(sys.argv) == 3 and sys.argv[1] == "--list":
        want = sys.argv[2]
        for prefix, icons in (("", material), ("fa:", fa)):
            for name, keywords in icons:
                if categorize(prefix + name, keywords) == want:
                    print(prefix + name)
        return
    counts = write_files(material, fa)
    for cat in [*CATEGORIES, FALLBACK]:
        print(f"{cat:20} {counts[cat]}")
    print(f"{'total':20} {sum(counts.values())}")


if __name__ == "__main__":
    main()
