package com.example.secretsanta.ui.feature.calendar

import java.time.LocalDate

object ChristmasProverbs {
    private val proverbs = listOf(
        "🎄 Noël au balcon, Pâques au tison",
        "❄️ Un Noël sans neige, un été sans soleil",
        "🎅 Qui donne aux pauvres prête à Dieu",
        "⭐ La générosité est le plus beau cadeau",
        "🕯️ Une petite lumière chasse les ténèbres",
        "🎁 Le meilleur cadeau est celui du cœur",
        "🌟 Noël est dans le cœur avant d'être sous le sapin",
        "🔔 La joie partagée est une joie doublée",
        "🎊 Donner c'est recevoir",
        "🌲 Noël n'est pas un jour mais un état d'esprit",
        "💫 Les petits plaisirs font les grands bonheurs",
        "🎀 La famille est le plus beau cadeau",
        "🌠 Chaque jour est une nouvelle chance",
        "🎵 La musique adoucit les mœurs",
        "🍪 Partager c'est aimer",
        "🧦 Les petites attentions font les grandes amitiés",
        "🎶 Le rire est contagieux, propagez-le !",
        "☃️ L'hiver est magique quand on le partage",
        "🌨️ Chaque flocon est unique comme chaque personne",
        "🎺 La tradition unit les générations",
        "🎉 Célébrons ensemble la magie de Noël",
        "🎈 L'enfance ne se mesure pas en années",
        "🎪 La joie est le soleil de l'âme",
        "🌹 Un sourire coûte peu mais vaut beaucoup",
        "🎭 Soyez vous-même, les autres sont déjà pris",
        "🎨 La créativité est l'intelligence qui s'amuse",
        "📚 Chaque jour est une page blanche",
        "🎯 Les rêves sont les étoiles de la vie",
        "🎡 Le bonheur est un voyage, pas une destination",
        "🎢 Vivez l'instant présent",
        "🎠 L'espoir fait vivre"
    )

    fun getProverbForDate(date: LocalDate): String {
        // Utilise le jour de l'année pour avoir un proverbe cohérent
        val dayOfYear = date.dayOfYear
        val index = (dayOfYear - 1) % proverbs.size
        return proverbs[index]
    }
}